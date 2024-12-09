package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigLosAngeles {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // '09/21/2023 07:18:00 AM'
    public static String REPORTED_AT_CSV_CRIME_LA(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Date Rptd");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // '09/21/2023 07:18:00 AM'
    public static String REPORTED_AT2_CSV_CRIME_LA(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("DATE OCC");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 2024-01-01T00:06:54.000
    public static String REPORTED_AT_311_LA(JsonNode item) {
        String dateStr = null;
        try {
            String text = item.at("/createddate").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

}
