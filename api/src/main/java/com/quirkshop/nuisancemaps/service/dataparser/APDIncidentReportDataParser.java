package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
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

    /*
     * custom parser for apd incident report source
     */

    @Override
    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        // sanity checks
        int numRows = 0;
        int numBatch = 0;

        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        /*
         * collect "rows"
         * NB: first row is outlier
         */

        List<Element> elements = buildElements(dataJob, inputStream);

        System.out.println("--------");

        for (Element element : elements) {

            List<Map<String, String>> rows = buildRowMap(element);

            numRows += rows.size();

            for (Map<String, String> row : rows) {
                // TODO: collect to single list - flatten
                // TODO: fix row data
                // send for enrichment before buildDataEntity
            }

        }

        System.out.println("COUNT: " + numRows);

    }

    /*
     * takes html element to create a "row" entity
     */
    public List<Map<String, String>> buildRowMap(Element element) {

        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();

        try {

            String reportNum = element.select("tr:nth-of-type(1) td:nth-of-type(2)").text();

            String reportDate = element.select("tr:nth-of-type(1) td:nth-of-type(4)").text();

            String offenseDate = element.select("tr:nth-of-type(3) td:nth-of-type(2)").text();

            String location = element.select("tr:nth-of-type(7) td:nth-of-type(2) p:nth-of-type(1)").text();

            int reportNumCounter = 1;
            Elements offensesTD = element.select("tr:nth-of-type(5) td");

            // fix: see 2024-2141103
            for (int i = 1; i < offensesTD.size(); i += 2) {

                Map<String, String> row = new HashMap<String, String>();

                String reportCategory = offensesTD.get(i).text().trim();

                row.put("reportNum", reportNum + "-" + String.valueOf(reportNumCounter));
                row.put("reportCategory", reportCategory);
                row.put("location", location);

                // NB: missing lat / lng, needs this data set needs enrichment

                row.put("reportedAt", reportDate);
                row.put("reportedAt2", offenseDate);

                reportNumCounter++;

                rows.add(row);
                System.out.println(row);
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
}
