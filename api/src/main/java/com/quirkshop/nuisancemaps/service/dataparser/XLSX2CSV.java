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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
@SuppressWarnings({ "java:S106", "java:S4823", "java:S1192" })
public class XLSX2CSV {
    /**
     * Uses the XSSF Event SAX helpers to do most of the work
     * of parsing the Sheet XML, and outputs the contents
     * as a (basic) CSV.
     */
    private class SheetToCSV implements SheetContentsHandler {
        private boolean firstCellOfRow;
        private int currentRow = -1;
        private int currentCol = -1;
        private ArrayList<String> headers = new ArrayList<String>();
        private HashMap<String, String> row = new HashMap<String, String>();

        private DataJob dataJob;
        private ParseCounter parseCounter;

        public SheetToCSV(DataJob dataJob, ParseCounter parseCounter) {
            this.dataJob = dataJob;
            this.parseCounter = parseCounter;
        }

        private void outputMissingRows(int number) {
            for (int i = 0; i < number; i++) {
                for (int j = 0; j < minColumns; j++) {
                    // output.append(',');
                }
                // output.append('\n');
            }
        }

        @Override
        public void startRow(int rowNum) {
            // rowNum starts at 0

            // If there were gaps, output the missing rows
            outputMissingRows(rowNum - currentRow - 1);
            // Prepare for this row
            firstCellOfRow = true;
            currentRow = rowNum;
            currentCol = -1;
        }

        @Override
        public void endRow(int rowNum) {
            // Ensure the minimum number of columns
            for (int i = currentCol; i < minColumns; i++) {
                // output.append(',');
            }
            // output.append('\n');

            // build if not headers
            if (rowNum == 0 || rowNum < dataJob.getParamOffset())
                return;

            // PARSE
            for (String header : headers) {
                System.out.println(header + ": " + row.getOrDefault(header, null));
            }
            System.out.println("---");
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {

            if (firstCellOfRow) {
                firstCellOfRow = false;
            } else {
                // output.append(',');
            }

            // SKIP offset (but make sure to capture headers)
            // NB: rowNum starts at 0 (typically headers)
            if (currentRow != 0 && currentRow < dataJob.getParamOffset()) {
                return;
            }

            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if (cellReference == null) {
                cellReference = new CellAddress(currentRow, currentCol).formatAsString();
            }

            // Did we miss any cells?
            int thisCol = (new CellReference(cellReference)).getCol();
            int missedCols = thisCol - currentCol - 1;
            for (int i = 0; i < missedCols; i++) {
                // output.append(',');
            }

            // no need to append anything if we do not have a value
            if (formattedValue == null) {
                return;
            }

            currentCol = thisCol;

            // TODO convert to Date
            // output.append("ROW: " + currentRow + " | COL: " + currentCol);
            // Number or string?
            try {
                // Number
                // noinspection ResultOfMethodCallIgnored
                Double.parseDouble(formattedValue);
                // output.append(formattedValue);

            } catch (Exception e) {
                // String
                // let's remove quotes if they are already there
                if (formattedValue.startsWith("\"") && formattedValue.endsWith("\"")) {
                    formattedValue = formattedValue.substring(1, formattedValue.length() - 1);
                }

                // output.append('"');
                // encode double-quote with two double-quotes to produce a valid CSV format
                // output.append(formattedValue.replace("\"", "\"\""));
                formattedValue = formattedValue.replace("\"", "\"\"");
                // output.append('"');

            }

            if (currentRow == 0) {
                // popuplate header
                headers.add(formattedValue);
            } else {
                String header = headers.get(currentCol);
                row.put(header, formattedValue);
            }
        }
    }

    ///////////////////////////////////////

    private final OPCPackage xlsxPackage;

    /**
     * Number of columns to read starting with leftmost
     */
    private final int minColumns;

    /**
     * Destination for data
     */
    private final PrintStream output;

    /**
     * Creates a new XLSX -&gt; CSV converter
     *
     * @param pkg        The XLSX package to process
     * @param output     The PrintStream to output the CSV to
     * @param minColumns The minimum number of columns to output, or -1 for no
     *                   minimum
     */
    public XLSX2CSV(OPCPackage pkg, PrintStream output, int minColumns) {
        this.xlsxPackage = pkg;
        this.output = output;
        this.minColumns = minColumns;
    }

    public XLSX2CSV() {
        this.xlsxPackage = null;
        this.output = null;
        this.minColumns = -1;
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
    // TODO: reduce to one shit (continue?)
    public void parse(DataJob dataJob, File file, InputStream inputStream, ParseCounter parseCounter)
            throws IOException, OpenXML4JException, SAXException {
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
                    // this.output.println();
                    // this.output.println(sheetName + " [index=" + index + "]:");

                    try {
                        processSheet(styles, strings, new SheetToCSV(dataJob, parseCounter), stream);
                    } catch (NumberFormatException e) {
                        throw new IOException("Failed to parse sheet " + sheetName, e);
                    }
                }
                ++index;
            }
        } finally {
            p.revert();
        }

    }
}
