package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigDenver {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 10/14/2020 10:20:00 PM
    public static String REPORTED_AT_CSV_CRIME_DENVER(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("REPORTED_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 12/31/2023 10:40:02 AM
    public static String REPORTED_AT_CSV_311_DENVER(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Case_Created_dttm");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

}
