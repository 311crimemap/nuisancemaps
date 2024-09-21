package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Boston
 */

public class ParserStrategyConfigBoston {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // LocalDateTime.parse requires ISO format but field is a simple date with 24 hr
    // time and timezone offset:
    // 2020-12-31 20:30:00+00
    public static String REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("OCCURRED_ON_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");

            // Parse the input string to a ZonedDateTime
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(text, formatter);

            // Convert ZonedDateTime to LocalDateTime
            LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = localDateTime.format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // field is a simple date with 24 hr time
    // 2020-12-31 20:30:00
    public static String REPORTED_AT_CSV_CRIME_BOSTON(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("OCCURRED_ON_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 2020-12-31 20:30:00
    public static String REPORTED_AT_CSV_311_BOSTON(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("open_dt");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

}
