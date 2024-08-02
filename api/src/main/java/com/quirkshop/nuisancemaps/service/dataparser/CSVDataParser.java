package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVReaderHeaderAware;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CSVDataParser extends DataParser {

    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        try {

            CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader);
            Map<String, String> headers = csvReader.readMap(); // Read the first row which contains headers

            Map<String, String> row;
            while ((row = csvReader.readMap()) != null) {

                try {

                    IDataEntity dataEntity = dataEntityMappingService
                            .buildDataEntity(dataEntityClass, source, row, geometryFactory);

                    addDataEntity(dataEntity, parseCounter);

                } catch (MissingCoordinateException | MissingReportCategoryException e) {
                    String content = StringUtils.substring(row.toString(), 0, 4096);
                    logMissingException(source, content, e);

                } catch (Exception e) {
                    String content = StringUtils.substring(row.toString(), 0, 4096);
                    logException(dataJob, content, e);
                    parseCounter.numErrorsIncrement();
                }

                parseCounter.numFetchedIncrement();

                if (reportNums.size() > BATCH_SIZE) {
                    batchSave(source, parseCounter);
                }

            }

            // flush remaining
            batchSave(source, parseCounter);

            csvReader.close();

        } catch (Exception e) {

        }

    }

    public JsonNode parseData(DataJob dataJob, InputStream inputStream) {
        // TODO Auto-generated method stub
        return null;
    }
}
