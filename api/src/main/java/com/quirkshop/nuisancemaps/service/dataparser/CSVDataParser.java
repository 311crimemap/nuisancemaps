package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class CSVDataParser extends DataParser {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    MapFieldExtractor mapFieldExtractor;

    @Override
    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        // sanity checks
        int numRows = 0;
        int numBatch = 0;

        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        try (CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader)) {

            csvReader.skip(dataJob.getParamOffset());

            if (dataJob.getParamOffset() > 0) {
                log.info(String.format("[CSVDataParser]: offset detected skipping %d lines",
                        dataJob.getParamOffset()));
            }

            Map<String, String> row;

            while (true) {

                try {
                    row = csvReader.readMap();
                } catch (CsvException | IOException e) {
                    // handle bad row; improper number of columns vs. headers, etc.
                    log.info("[CSVDataParser]: " + e.getMessage());
                    parseCounter.numRowErrorsIncrement();
                    continue;
                }

                // EOF
                if (row == null)
                    break;

                // PARSE
                try {

                    IDataEntity dataEntity = dataEntityMappingService
                            .buildDataEntity(dataEntityClass, source, row, geometryFactory, mapFieldExtractor);

                    addDataEntity(dataEntity, parseCounter);

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
                    logSaveBatch(dataJob, parseCounter, csvReader, numBatch, numRows);
                }

                parseCounter.numFetchedIncrement();
                numRows++;
            }

            numBatch++;
            logSaveBatch(dataJob, parseCounter, csvReader, numBatch, numRows);

        } catch (Exception e) {
            log.info("[CSVDataParser] parse ERR: " + e.getMessage());
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.ERROR);
            dataJobRepository.save(dataJob);
        }

    }

    private void logSaveBatch(DataJob dataJob, ParseCounter parseCounter, CSVReaderHeaderAware csvReader,
            int numBatch, int numRows) {

        batchSave(dataJob.getSource(), parseCounter);

        log.info(String.format("[CSVDataParser] dataJob: %d | numBatch: %d | numRows: %d",
                dataJob.getId(), numBatch, numRows));

        log.info(String.format(
                "[CSVDataParser] dataJob: %d | linesRead: %d, recordsRead: %d, skipLines: %d, multiLineLimit: %d",
                dataJob.getId(),
                csvReader.getLinesRead(),
                csvReader.getRecordsRead(),
                csvReader.getSkipLines(),
                csvReader.getMultilineLimit()));

        // update offset for possible restart
        dataJob.setParamOffset((int) csvReader.getLinesRead());
        dataJobRepository.save(dataJob);
    }

}
