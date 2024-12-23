package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/*
 * DEPRECATED
 * This parser consumes insane amounts of memory (heap) for even reasonably
 * sized workbooks (e.g. 25MB)
 */

@Service
@Scope("prototype")
public class XLSDataParser extends DataParser {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    MapFieldExtractor mapFieldExtractor;

    @Override
    public void parse(DataJob dataJob, File file, InputStream inputStream, ParseCounter parseCounter) {
        // sanity checks
        int numRows = 0;
        int numBatch = 0;

        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        HashSet<String> pendingReportCategories = new HashSet<String>();

        try (Workbook workbook = WorkbookFactory.create(file)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<String[]> data = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            /*
             * Headers
             */

            Row headerRow = sheet.getRow(0);
            String[] headerRowData = new String[headerRow.getLastCellNum()];

            // cell
            for (int cn = 0; cn < headerRow.getLastCellNum(); cn++) {
                Cell cell = headerRow.getCell(cn);
                headerRowData[cn] = cell == null ? "" : formatter.formatCellValue(cell);
            }

            if (dataJob.getParamOffset() > 0) {
                log.info(String.format("[XLSDataParser]: offset detected skipping %d lines",
                        dataJob.getParamOffset()));
            }

            // row iteration
            HashMap<String, String> row = new HashMap<String, String>();
            log.info("Row Loop Start");
            // skip header or skip to offset (plus 1 for header)
            for (int i = dataJob.getParamOffset() + 1; i <= sheet.getLastRowNum(); i++) {

                try {
                    Row sheetRow = sheet.getRow(i);
                    String[] rowData = new String[sheetRow.getLastCellNum()];

                    // build 'row': single header:cell value pair
                    for (int j = 0; j < sheetRow.getLastCellNum(); j++) {
                        Cell cell = sheetRow.getCell(j);
                        rowData[j] = cell == null ? "" : formatter.formatCellValue(cell);

                        if (cell != null &&
                                cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

                            // Convert the cell value to a LocalDateTime
                            LocalDateTime dateTime = cell.getDateCellValue()
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();

                            // Format it using ISO_LOCAL_DATE_TIME
                            String formattedDate = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            row.put(headerRowData[j], formattedDate);
                        } else {
                            row.put(headerRowData[j], rowData[j]);
                        }
                    }

                } catch (Exception e) {
                    // handle bad row; improper number of columns vs. headers, etc.
                    log.info("[XLSDataParser] row error: " + e.getMessage());
                    parseCounter.numRowErrorsIncrement();
                    continue;
                }

                // PARSE

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
                    log.info("[XLSDataParser] row: " + numRows);
                    logException(dataJob, content, e);
                    parseCounter.numErrorsIncrement();
                }

                if (reportNums.size() >= BATCH_SIZE) {
                    numBatch++;
                    logSaveBatch(dataJob, parseCounter, numBatch, numRows);
                }

                parseCounter.numFetchedIncrement();
                numRows++;

            } // end for

            numBatch++;
            logSaveBatch(dataJob, parseCounter, numBatch, numRows);

        } catch (Exception e) {

            log.info("[XLSDataParser] parse ERR: " + e.getMessage());
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.ERROR);
            dataJobRepository.save(dataJob);
        }

        savePendingTextCategories(dataJob, source, pendingReportCategories);
    }

    private void logSaveBatch(DataJob dataJob, ParseCounter parseCounter, int numBatch, int numRows) {

        batchSave(dataJob.getSource(), parseCounter);

        log.info(String.format("[XLSDataParser] dataJob: %d | numBatch: %d | numRows: %d",
                dataJob.getId(), numBatch, numRows));

        // update offset for possible restart
        dataJob.setParamOffset(dataJob.getParamOffset() + numRows);
        dataJobRepository.save(dataJob);
    }

}
