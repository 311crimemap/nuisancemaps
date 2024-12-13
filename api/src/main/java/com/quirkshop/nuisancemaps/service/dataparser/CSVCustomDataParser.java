package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.quirkshop.nuisancemaps.config.InvalidCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.Mapping;
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
public class CSVCustomDataParser extends DataParser {

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

        Mapping mapping = source.getMapping();
        Character delimeter = mapping.getDataParserDelimeter().charAt(0);
        Integer initialNumSkip = mapping.getDataParserNumSkip();

        textCategoryService.refreshTextCategoryIdMap();

        HashSet<String> pendingReportCategories = new HashSet<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        try (CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(delimeter).build())
                .build()) {

            csvReader.skip(initialNumSkip + dataJob.getParamOffset());

            if (initialNumSkip + dataJob.getParamOffset() > 0) {
                log.info(String.format("[CSVCustomDataParser]: offset detected skipping %d + %d = %d lines",
                        initialNumSkip, dataJob.getParamOffset(),
                        initialNumSkip + dataJob.getParamOffset()));
            }

            // Map<String, String> trimmedRow;
            String[] headers = null;
            Map<String, String> rowMap = new HashMap<>();
            for (String[] row : csvReader) {

                // build headers
                if (headers == null) {
                    for (int i = 0; i < row.length; i++) {
                        row[i] = clean(row[i]);
                    }

                    headers = row;
                    continue;
                }

                // populate each row
                for (int i = 0; i < headers.length; i++) {

                    try {
                        rowMap.put(headers[i], i < row.length ? row[i] : null);
                    } catch (Exception e) {

                        // handle bad row; improper number of columns vs. headers, etc.
                        log.info("[CSVDataParser]: " + e.getMessage());
                        parseCounter.numRowErrorsIncrement();
                        continue;
                    }
                }

                // PARSE

                try {

                    DataEntity dataEntity = dataEntityMappingService
                            .buildDataEntity(dataEntityClass, source, rowMap, geometryFactory, mapFieldExtractor);

                    addDataEntity(dataEntity, parseCounter);

                } catch (MissingCategoryException e) {

                    pendingReportCategories.add(e.getReportCategory());
                    parseCounter.numMissingIncrement();

                } catch (InvalidCoordinateException | MissingCoordinateException | MissingReportCategoryException e) {
                    String content = StringUtils.substring(row.toString(), 0, 4096);
                    logMissingException(source, content, e);
                    parseCounter.numMissingIncrement();

                } catch (Exception e) {
                    String content = StringUtils.substring(row.toString(), 0, 4096);
                    log.info("[CSVCustomDataParser] row: " + numRows);
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
            log.info("[CSVCustomDataParser] parse ERR: " + e.getMessage());
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.ERROR);
            dataJobRepository.save(dataJob);
        }

        savePendingTextCategories(dataJob, source, pendingReportCategories);
    }

    private String clean(String str) {
        return str.replaceAll("\uFEFF", "") // BOM
                .replaceAll("\u00A0", "") // non-breaking spaces (shouldn't be an issue but)
                .replaceAll("\u200B", "") // zero-width spaces
                .trim(); // Standard trim
    }

    private void logSaveBatch(DataJob dataJob, ParseCounter parseCounter, CSVReader csvReader,
            int numBatch, int numRows) {

        batchSave(dataJob.getSource(), parseCounter);

        log.info(String.format("[CSVCustomDataParser] dataJob: %d | numBatch: %d | numRows: %d",
                dataJob.getId(), numBatch, numRows));

        log.info(String.format(
                "[CSVCustomDataParser] dataJob: %d | linesRead: %d, recordsRead: %d, skipLines: %d, multiLineLimit: %d",
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
