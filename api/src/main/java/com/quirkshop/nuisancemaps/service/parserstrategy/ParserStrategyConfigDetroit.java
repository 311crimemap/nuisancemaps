package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Detroit
 */

public class ParserStrategyConfigDetroit {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 2024/12/16 00:25:00+00
    public static String REPORTED_AT_CSV_CRIME_DETROIT(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("incident_occurred_at");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssx");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_CRIME_DETROIT] " + e.getMessage());
        }

        return dateStr;
    }

    // 2024/11/25 12:34:56+00
    public static String REPORTED_AT_CSV_311_DETROIT(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Created_At");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssx");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_311_DETROIT] " + e.getMessage());
        }

        return dateStr;
    }

}
