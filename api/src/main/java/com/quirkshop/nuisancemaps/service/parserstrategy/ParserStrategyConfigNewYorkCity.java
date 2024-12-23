package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * New York City
 */

public class ParserStrategyConfigNewYorkCity {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // LocalDateTime.parse requires ISO format but field is a simple date
    // (MM/DD/YYYY) - only for CSV (but not JSON)
    public static String REPORTED_AT_CSV_CRIME_NEW_YORK_CITY(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("RPT_DT");

            if (text.isBlank())
                return null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_CRIME_NEW_YORK_CITY] " + e.getMessage());
        }

        return dateStr;
    }

    public static String REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("CMPLNT_FR_DT");

            if (text.isBlank())
                return null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY] " + e.getMessage());
        }

        return dateStr;
    }

    // 09/18/2024 02:24:09 AM
    public static String REPORTED_AT_CSV_311_NEW_YORK_CITY(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Created Date");

            if (text.isBlank())
                return null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_311_NEW_YORK_CITY] " + e.getMessage());
        }

        return dateStr;
    }

}
