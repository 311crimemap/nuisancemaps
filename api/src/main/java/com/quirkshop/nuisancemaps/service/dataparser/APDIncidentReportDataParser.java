package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.service.GeocoderService;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class APDIncidentReportDataParser extends DataParser {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    MapFieldExtractor mapFieldExtractor;

    @Autowired
    GeocoderService geocoderService;

    /*
     * custom parser for apd incident report source
     */

    @Override
    public void parse(DataJob dataJob, File file, InputStream inputStream, ParseCounter parseCounter) {

        log.info(String.format("[APDIncidentReport] parse() | DataJob: %s", dataJob.getId()));

        // sanity checks
        int numRows = 0;
        int numBatch = 0;

        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        HashSet<String> pendingReportCategories = new HashSet<String>();

        List<Element> elements = buildElements(dataJob, inputStream);

        if (elements.size() == 0)
            return;

        List<Map<String, String>> rows = parseToRowMaps(elements);

        geocode(source, rows);

        // send to buildDataEntity

        for (Map<String, String> row : rows) {
            // PARSE
            try {

                DataEntity dataEntity = dataEntityMappingService
                        .buildDataEntity(dataEntityClass, source, row, geometryFactory, mapFieldExtractor);

                addDataEntity(dataEntity, parseCounter);

            } catch (MissingCategoryException e) {

                pendingReportCategories.add(e.getReportCategory());
                parseCounter.numMissingIncrement();

            } catch (MissingCoordinateException | MissingReportCategoryException e) {
                String content = StringUtils.substring(row.toString(), 0, 4096);
                logMissingException(source, content, e);
                parseCounter.numMissingIncrement();

            } catch (Exception e) {
                String content = StringUtils.substring(row.toString(), 0, 4096);
                logException(dataJob, content, e);
                parseCounter.numErrorsIncrement();
            }

            if (reportNums.size() >= BATCH_SIZE) {
                numBatch++;
                logSaveBatch(dataJob, parseCounter, numBatch, numRows);
            }

            parseCounter.numFetchedIncrement();
            numRows++;
        }

        numBatch++;
        logSaveBatch(dataJob, parseCounter, numBatch, numRows);

        savePendingTextCategories(dataJob, source, pendingReportCategories);
    }

    public List<Map<String, String>> parseToRowMaps(List<Element> elements) {

        log.info(String.format("[APDIncidentReport] parseToRowMaps()"));

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for (Element element : elements) {

            List<Map<String, String>> rows = buildRowMap(element);

            for (Map<String, String> row : rows) {
                data.add(row);
            }
        }

        return data;
    }

    public void geocode(Source source, List<Map<String, String>> data) {

        log.info(String.format("[APDIncidentReport] geocode()"));

        // batch send for geocoding
        List<String> addresses = new ArrayList<String>();
        for (Map<String, String> row : data) {
            addresses.add(row.get("address"));
        }

        List<double[]> coordinates = geocoderService.geocode(source, addresses);

        if (data.size() != coordinates.size()) {
            String err = String.format("Address count: %d does not match coordinate counts: %d",
                    data.size(), coordinates.size());
            throw new Error(err);
        }

        // add data points
        for (int i = 0; i < coordinates.size(); i++) {

            Map<String, String> row = data.get(i);
            double[] latlng = coordinates.get(i);

            if (latlng != null) {
                row.put("latitude", String.valueOf(latlng[0]));
                row.put("longitude", String.valueOf(latlng[1]));
            }

        }
    }

    // remove apt component if it exists, and any excess spaces
    public String formatAddress(String address) {
        String[] splits = address.split(",");

        if (splits.length == 2) {
            return address
                    .replaceAll("\\s+", " ");
        }

        // remove apt case throws off geocoding
        if (splits.length == 3) {
            return String.join(",", splits[0], splits[2])
                    .replaceAll("\\s+", " ");
        }

        return address;
    }

    /*
     * takes html element to create a "row" entity
     */
    public List<Map<String, String>> buildRowMap(Element element) {

        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();

        try {

            String reportNum = element.select("tr:nth-of-type(1) td:nth-of-type(2)").text().trim();

            String reportDate = element.select("tr:nth-of-type(1) td:nth-of-type(4)").text().trim();

            String offenseDate = element.select("tr:nth-of-type(3) td:nth-of-type(2)").text().trim();

            String address = element.select("tr:nth-of-type(7) td:nth-of-type(2) p:nth-of-type(1)").text().trim();
            address = formatAddress(address); // dropping apt

            int reportNumCounter = 1;
            Elements offensesTD = element.select("tr:nth-of-type(5) td:nth-of-type(2) td");

            for (int i = 0; i < offensesTD.size(); i++) {

                Map<String, String> row = new HashMap<String, String>();

                String reportCategory = offensesTD.get(i).text()
                        .replaceAll("\u00A0", " ")
                        .trim();

                row.put("reportNum", reportNum + "-" + String.valueOf(reportNumCounter));
                row.put("reportCategory", reportCategory);
                row.put("address", address);

                // NB: data set needs enrichment to get lat/lng
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM-dd-yyyy HH:mm");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                row.put("reportedAt", LocalDateTime.parse(offenseDate, formatter).format(outputFormatter));
                row.put("reportedAt2", LocalDateTime.parse(reportDate, formatter).format(outputFormatter));

                reportNumCounter++;

                rows.add(row);
                // System.out.println(row);
            }

        } catch (Exception e) {

            log.info("[APDHTMLDataParser] Parse Err: " + e.getMessage());
            log.info(element.html());
            log.info("--------\n");
        }

        return rows;

    }

    /*
     * custom parsing selectors to extract a list of "elements", each
     * representing a single entity
     */
    public List<Element> buildElements(DataJob dataJob, InputStream inputStream) {

        List<Element> elements = new ArrayList<Element>();

        try {
            Document document = Jsoup.parse(inputStream, "UTF-8", "");

            Elements tables = document.select("div.container > table");

            // results take some time to enter system; very possible for
            // near-term dates to return no results
            if (tables.size() == 0) {
                return elements;
            }

            // first element nested outlier - needs additional selector
            Element firstNestedTable = tables.get(0).selectFirst("tr table");
            elements.add(firstNestedTable);

            // remainder seem to follow every 3rd table
            for (int i = 1; i < tables.size(); i += 3) {
                Element table = tables.get(i).select("tbody").first();
                elements.add(table);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return elements;
    }

    private void logSaveBatch(DataJob dataJob, ParseCounter parseCounter, int numBatch, int numRows) {

        batchSave(dataJob.getSource(), parseCounter);

        log.info(String.format("[APDIncidentReportDataParser] dataJob: %d | numBatch: %d | numRows: %d",
                dataJob.getId(), numBatch, numRows));

        dataJobRepository.save(dataJob);
    }

}
