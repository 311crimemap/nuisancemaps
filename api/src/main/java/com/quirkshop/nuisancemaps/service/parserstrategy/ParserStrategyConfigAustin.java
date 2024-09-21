package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigAustin {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String STREET_NAME_ERSI_AUSTIN(JsonNode item) {
        String value = null;

        try {
            String addressBlock = item.at("/attributes/ADDRESS_BLOCK").asText();
            String streetName = item.at("/attributes/STREET_NAME").asText();
            String streetType = item.at("/attributes/STREET_TYPE").asText();
            value = String.join(" ", addressBlock, streetName, streetType);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return value;
    }

    public static String OCCURRENCE_DATE_ERSI_AUSTIN(JsonNode item) {
        String dateStr = null;

        try {
            long occurrenceDate = item.at("/attributes/OCCURRENCE_DATE").asLong();
            long occurenceTime = item.at("/attributes/OCCURRENCE_TIME").asLong();

            // use epoch to get GMT date (e.g midnight of that day)
            // occurrence time for that GMT date - example: 824, 1352.
            LocalDateTime date = LocalDateTime.ofEpochSecond(occurrenceDate / 1000, 0, ZoneOffset.UTC)
                    .withHour((int) occurenceTime / 100)
                    .withMinute((int) occurenceTime % 100);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = date.format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // '09/21/2023 07:18:00 AM'
    public static String CREATED_DATE_CSV_AUSTIN(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Created Date");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error("CREATED_DATE_CSV_AUSTIN: " + row.get("Created Date") + " | " + e.getMessage());
        }

        return dateStr;
    }

    // '09/21/2023 07:18:00 AM'
    public static String REPORTED_AT_CSV_AUSTIN(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Occurred Date Time");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error("REPORTED_AT_CSV_AUSTIN: " + row.get("Occurred Date Time") + " | " + e.getMessage());
        }

        return dateStr;
    }

    // '09/21/2023 07:18:00 AM'
    public static String REPORTED_AT2_CSV_AUSTIN(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Report Date Time");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error("REPORTED_AT2_CSV_AUSTIN: " + row.get("Report Date Time") + " | " + e.getMessage());
        }

        return dateStr;
    }

}
