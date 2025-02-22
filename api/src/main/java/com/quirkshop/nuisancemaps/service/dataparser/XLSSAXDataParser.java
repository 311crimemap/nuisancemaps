
/*
 * Need to use sax parser to avoid high memory usage parsing excel:
 * https://poi.apache.org/components/spreadsheet/limitations.html
 * https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api
 *
 * This parser ported from example XLSX2CSV.java:
 * https://svn.apache.org/repos/asf/poi/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/XLSX2CSV.java
 *
 * NB: only works with .xlsx (not older .xls)
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import com.quirkshop.nuisancemaps.config.InvalidCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.config.ThresholdReportedAtException;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.logstash.logback.argument.StructuredArguments;

/**
 * A rudimentary XLSX -&gt; CSV processor modeled on the
 * POI sample program XLS2CSVmra from the package
 * org.apache.poi.hssf.eventusermodel.examples.
 * As with the HSSF version, this tries to spot missing
 * rows and cells, and output empty entries for them.
 * <p>
 * Data sheets are read using a SAX parser to keep the
 * memory footprint relatively small, so this should be
 * able to read enormous workbooks. The styles table and
 * the shared-string table must be kept in memory. The
 * standard POI styles table class is used, but a custom
 * (read-only) class is used for the shared string table
 * because the standard POI SharedStringsTable grows very
 * quickly with the number of unique strings.
 * <p>
 * For a more advanced implementation of SAX event parsing
 * of XLSX files, see {@link XSSFEventBasedExcelExtractor}
 * and {@link XSSFSheetXMLHandler}. Note that for many cases,
 * it may be possible to simply use those with a custom
 * {@link SheetContentsHandler} and no SAX code needed of
 * your own!
 */

@Service
@Scope("prototype")
@SuppressWarnings({ "java:S106", "java:S4823", "java:S1192" })
public class XLSSAXDataParser extends DataParser {
    /**
     * Uses the XSSF Event SAX helpers to do most of the work
     * of parsing the Sheet XML, and outputs the contents
     * as a (basic) CSV.
     */
    private class SheetToCSV implements SheetContentsHandler {
        private int currentRow = -1;
        private int currentCol = -1;
        private ArrayList<String> headers = new ArrayList<String>();
        private HashMap<String, String> row = new HashMap<String, String>();

        private DataJob dataJob;
        private Source source;
        private ParseCounter parseCounter;

        private int numBatch = 0;

        public SheetToCSV(DataJob dataJob, ParseCounter parseCounter) {
            this.dataJob = dataJob;
            this.parseCounter = parseCounter;
            this.source = dataJob.getSource();

            if (dataJob.getParamOffset() > 0) {
                Map<String, Object> logDetails = Map.of("offsetSkipped", dataJob.getParamOffset());
                log.info("[XLSSAXDataParser] offset detected skipping lines",
                        StructuredArguments.entries(Map.of("data", logDetails)));
            }
        }

        @Override
        public void startRow(int rowNum) {
            // rowNum starts at 0
            currentRow = rowNum;
            currentCol = -1;
        }

        @Override
        public void endRow(int rowNum) {

            // build if not headers
            if (rowNum == 0 || rowNum < dataJob.getParamOffset())
                return;

            // PARSE

            // for (String header : headers) {
            // System.out.println(header + ": " + row.getOrDefault(header, null));
            // }
            // System.out.println("---");

            try {

                DataEntity dataEntity = dataEntityMappingService
                        .buildDataEntity(dataEntityClass, source, row, geometryFactory,
                                mapFieldExtractor);

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
                log.info("[XLSSAXDataParser]",
                        StructuredArguments.entries(Map.of("data", Map.of("row", currentRow))));
                logException(dataJob, content, e);
                parseCounter.numErrorsIncrement();
            }

            if (reportNums.size() >= BATCH_SIZE) {
                logSaveBatch(dataJob, parseCounter);
            }

            parseCounter.numFetchedIncrement();
        }

        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            // SKIP offset (but make sure to capture headers)
            // NB: rowNum starts at 0 (typically headers)
            if (currentRow != 0 && currentRow < dataJob.getParamOffset()) {
                return;
            }

            // no need to append anything if we do not have a value
            if (formattedValue == null) {
                return;
            }

            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if (cellReference == null) {
                cellReference = new CellAddress(currentRow, currentCol).formatAsString();
            }

            currentCol = (new CellReference(cellReference)).getCol();

            // EXTRACT
            // Note: can't get Cell type with SAX parser approach.
            // Issue with consistently handling Dates; for now deferring to custom Mapping
            // functions per source
            //
            // When opening and re-saving an xlsx, the actual date representation in the
            // cell can change
            // e.g. resaved to create a truncated test fixture, original data in parsing
            // changed from
            // 1/1/23 to 1/1/2023
            //

            if (currentRow == 0) {
                // build initial header
                headers.add(formattedValue);
            } else {
                String header = headers.get(currentCol);
                row.put(header, formattedValue);
            }

        }
    }

    ///////////////////////////////////////
    /**
     * Creates a new XLSX -&gt; CSV converter
     */

    HashSet<String> pendingReportCategories = new HashSet<String>();

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    MapFieldExtractor mapFieldExtractor;

    public XLSSAXDataParser() {
    }

    /**
     * Parses and shows the content of one sheet
     * using the specified styles and shared-strings tables.
     *
     * @param styles           The table of styles that may be referenced by cells
     *                         in the sheet
     * @param strings          The table of strings that may be referenced by cells
     *                         in the sheet
     * @param sheetInputStream The stream to read the sheet-data from.
     *
     * @throws java.io.IOException An IO exception from the parser,
     *                             possibly from a byte stream or character stream
     *                             supplied by the application.
     * @throws SAXException        if parsing the XML data fails.
     */
    public void processSheet(
            Styles styles,
            SharedStrings strings,
            SheetContentsHandler sheetHandler,
            InputStream sheetInputStream) throws IOException, SAXException {
        // set emulateCSV=true on DataFormatter - it is also possible to provide a
        // Locale
        // when POI 5.2.0 is released, you can call
        // formatter.setUse4DigitYearsInAllDateFormats(true)
        // to ensure all dates are formatted with 4 digit years
        DataFormatter formatter = new DataFormatter(true);
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = XMLHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                    styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }

    /**
     * Initiates the processing of the XLS workbook file to CSV.
     *
     * @throws IOException  If reading the data from the package fails.
     * @throws SAXException if parsing the XML data fails.
     */
    // TODO: reduce to one sheet (continue?)
    public void parse(DataJob dataJob, File file, InputStream inputStream, ParseCounter parseCounter) {

        int currentRow = 0;

        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        OPCPackage p = null;
        try {
            p = OPCPackage.open(file.getPath(), PackageAccess.READ);

            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(p);
            XSSFReader xssfReader = new XSSFReader(p);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            int index = 0;
            while (iter.hasNext()) {
                try (InputStream stream = iter.next()) {
                    String sheetName = iter.getSheetName();
                    try {
                        processSheet(styles, strings, new SheetToCSV(dataJob, parseCounter), stream);
                    } catch (NumberFormatException e) {
                        throw new IOException("Failed to parse sheet " + sheetName, e);
                    }
                }
                ++index;
            }
        } catch (Exception e) {
            log.info("[XLSSAXDataParser] SAX parse ERR: " + e.getMessage());
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.ERROR);
        } finally {
            dataJobRepository.save(dataJob);
            p.revert();
        }

        logSaveBatch(dataJob, parseCounter); // finish remaining set less than batch
        savePendingTextCategories(dataJob, source, pendingReportCategories);
    }

    private void logSaveBatch(DataJob dataJob, ParseCounter parseCounter) {
        int numRows = parseCounter.getNumFetched();

        batchSave(dataJob.getSource(), parseCounter);

        Map<String, Object> logDetails = Map.of(
                "dataJob", dataJob.getId(),
                "numBatch", parseCounter.getNumBatch(),
                "numRows", numRows);

        log.info("[XLSSAXDataParser] logSaveBatch",
                StructuredArguments.entries(Map.of("data", logDetails)));

        // update offset for possible restart
        dataJob.setParamOffset(numRows);
        parseCounter.setNumBatch(parseCounter.getNumBatch() + 1);
        dataJobRepository.save(dataJob);
    }

}
