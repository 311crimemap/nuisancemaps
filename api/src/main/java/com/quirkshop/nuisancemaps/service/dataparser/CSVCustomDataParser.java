package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.quirkshop.nuisancemaps.config.InvalidCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.config.ThresholdReportedAtException;
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

import net.logstash.logback.argument.StructuredArguments;

@Service
@Scope("prototype")
public class CSVCustomDataParser extends DataParser {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    MapFieldExtractor mapFieldExtractor;

    /*
     * Parsing Notes:
     *
     * &
     * NO_QUOTE_CHARACTER: don't care about properly enclosed quotes - it breaks
     * parsing, just keep rows consistent
     *
     * EOF extra blank lines can trigger noisy errors but overall parsing iteration
     * should recover
     *
     * Separate out csv iterator to catch and recover from bad parses on a row
     * basis. Bit more robust.
     *
     * Process:
     * 1. skip initial lines (specified by mapping.getDataParserNumSkip())
     * 2. "first" row assumed as headers; load
     * 3. skip subsequent rows according to paramOffset
     * 4. start parsing rows
     *
     */
    @Override
    public void parse(DataJob dataJob, File file, InputStream inputStream, ParseCounter parseCounter) {
        // sanity checks
        int numRows = 0;
        int numBatch = 0;

        Source source = dataJob.getSource();
        setTypes(source);

        Mapping mapping = source.getMapping();

        /*
         * default delimeter: ,
         * default quotechar: "
         * NB: '\\0' is a placeholder since we can't store null in psql
         */
        Character delimeter = mapping.getDataParserDelimeter().charAt(0);
        String dataParserQuote = mapping.getDataParserQuote();
        Character quote = dataParserQuote.equals("NULL") ? '\u0000' : dataParserQuote.charAt(0);
        Integer initialNumSkip = mapping.getDataParserNumSkip();

        textCategoryService.refreshTextCategoryIdMap();

        HashSet<String> pendingReportCategories = new HashSet<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        /*
         * Escape char :'\0' is null doesn't appear in csv
         * case where csv parsing hangs because a quote is escaped: \", this
         * turns the row into an endless open string. Avoid this by deliberately
         * changing the escape character to something else (null) so it
         * maintains parse-ability.
         *
         * withQuoteChar: default '"', alt using unicode null '\u0000
         * some csv's are missing closing quote to also creating endless string
         * (not only escaped like above) so can sometimes succesfully parse by
         * ignoring quotes.
         */

        try (CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(delimeter)
                        .withQuoteChar(quote)
                        .withEscapeChar('\0')
                        .build())
                .withMultilineLimit(2)
                .build()) {

            if (initialNumSkip > 0) {
                csvReader.skip(initialNumSkip);
                Map<String, Object> logDetails = Map.of("offsetSkipped", initialNumSkip);
                log.info("[CSVCustomDataParser] offset detected skipping lines",
                        StructuredArguments.entries(Map.of("data", logDetails)));
            }

            String[] headers = null;
            Map<String, String> rowMap = new HashMap<>();
            String[] row;

            Iterator<String[]> csvIter = csvReader.iterator();
            while (csvIter.hasNext()) {

                try {
                    row = csvIter.next();
                } catch (Exception e) {
                    // log.error("[CSVCustomDataParser] Iterator Row Parse ERR: " + e.getMessage());
                    parseCounter.numRowErrorsIncrement();
                    continue;
                }

                // build headers
                if (headers == null) {
                    for (int i = 0; i < row.length; i++) {
                        row[i] = clean(row[i]);
                    }

                    headers = row;

                    try {
                        csvReader.skip(dataJob.getParamOffset());
                    } catch (Exception e) {
                        Map<String, Object> logError = Map.of("error", e.getMessage());
                        log.error("[CSVCustomDataParser] ERR Row Skip",
                                StructuredArguments.entries(Map.of("data", logError)));
                        parseCounter.numRowErrorsIncrement();
                    }

                    continue;
                }

                // populate row with each column value
                boolean hasMismatchHeaderRowLenError = false;
                for (int i = 0; i < headers.length; i++) {

                    try {
                        // System.out.println(headers[i] + " | " + row[i]);
                        rowMap.put(headers[i], i < row.length ? row[i] : null);
                    } catch (Exception e) {
                        // typically header vs row column num mismatch:
                        // improper number of columns vs. headers, etc.
                        hasMismatchHeaderRowLenError = true;
                        continue;
                    }
                }

                // System.out.println("----------------------\n");

                if (hasMismatchHeaderRowLenError) {
                    Map<String, Object> logError = Map.of(
                            "headersLength", headers.length,
                            "rowLength", row.length);
                    log.error("[CSVCustomDataParser] ERR Row Skip",
                            StructuredArguments.entries(Map.of("data", logError)));
                    parseCounter.numRowErrorsIncrement();
                }

                // PARSE

                try {

                    DataEntity dataEntity = dataEntityMappingService
                            .buildDataEntity(dataEntityClass, source, rowMap, geometryFactory, mapFieldExtractor);

                    addDataEntity(dataEntity, parseCounter);

                } catch (MissingCategoryException e) {

                    pendingReportCategories.add(e.getReportCategory());
                    parseCounter.numMissingIncrement();

                } catch (InvalidCoordinateException | MissingCoordinateException
                        | MissingReportCategoryException e) {
                    String content = StringUtils.substring(row.toString(), 0, 4096);
                    logMissingException(source, content, e);
                    parseCounter.numMissingIncrement();

                } catch (ThresholdReportedAtException e) {
                    parseCounter.numExceededThresholdIncrement();
                } catch (Exception e) {
                    String content = StringUtils.substring(row.toString(), 0, 4096);
                    log.info("[CSVCustomDataParser]",
                            StructuredArguments.entries(Map.of("data", Map.of("row", numRows))));
                    logException(dataJob, content, e);
                    parseCounter.numErrorsIncrement();
                }

                if (reportNums.size() >= BATCH_SIZE) {
                    numBatch++;
                    logSaveBatch(dataJob, parseCounter, csvReader, numBatch, numRows);
                }

                parseCounter.numFetchedIncrement();
                numRows++;

            } // end row loop

            numBatch++;
            logSaveBatch(dataJob, parseCounter, csvReader, numBatch, numRows);

        } catch (Exception e) {
            Map<String, Object> logError = Map.of("error", e.getMessage(),
                    "stackTrace", e.getStackTrace());
            log.error("[CSVCustomDataParser] parse ERR",
                    StructuredArguments.entries(Map.of("data", logError)));
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

        Map<String, Object> logDetails1 = Map.of(
                "dataJobId", dataJob.getId(),
                "numBatch", numBatch,
                "numRows", numRows);

        log.info("[CSVCustomDataParser]", StructuredArguments.entries(Map.of("data", logDetails1)));

        Map<String, Object> logDetails2 = Map.of(
                "dataJobId", dataJob.getId(),
                "linesRead", csvReader.getLinesRead(),
                "recordsRead", csvReader.getRecordsRead(),
                "numMissing", parseCounter.getNumMissing(),
                "numExceedThreshold", parseCounter.getNumExceededThreshold(),
                "numRowErrors", parseCounter.getNumRowErrors(),
                "skipLines", csvReader.getSkipLines(),
                "multiLineLimit", csvReader.getMultilineLimit());

        log.info("[CSVCustomDataParser]", StructuredArguments.entries(Map.of("data", logDetails2)));

        // update offset for possible restart
        dataJob.setParamOffset((int) csvReader.getLinesRead());
        dataJobRepository.save(dataJob);
    }

}
