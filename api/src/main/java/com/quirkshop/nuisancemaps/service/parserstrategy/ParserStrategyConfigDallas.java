package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * DALLAS
 */

public class ParserStrategyConfigDallas {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String LATITUDE_311_DALLAS(JsonNode item, MappingField mappingField) {
        // {..., "lat_location": (32.77937339624264000,-96.85251201839743000), ...}
        String latitude = null;

        try {
            String text = item.at("/lat_location").asText();
            String coordinates = text.replaceAll("[()]", "");
            latitude = coordinates.split(",")[0];
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return latitude;
    }

    public static String LONGITUDE_311_DALLAS(JsonNode item, MappingField mappingField) {
        // {..., "lat_location": (32.77937339624264000,-96.85251201839743000), ...}
        String longitude = null;

        try {
            String text = item.at("/lat_location").asText();
            String coordinates = text.replaceAll("[()]", "");
            longitude = coordinates.split(",")[1];
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return longitude;
    }

    public static String LATITUDE_CSV_311_DALLAS(Map<String, String> row, MappingField mappingField) {
        // (32.71777362108976000,-96.80840102118572000)
        String latitude = null;

        try {

            String text = row.get("Lat_Long Location");

            // no coords equivalent
            if (text.equals("(,)"))
                return null;

            String coordinates = text.replaceAll("[()]", "");
            latitude = coordinates.split(",")[0];

        } catch (Exception e) {
            String text = row.get("Lat_Long Location");
            log.error("[LATITUDE_CSV_311_DALLAS] " + e.getMessage());
        }

        return latitude;
    }

    public static String LONGITUDE_CSV_311_DALLAS(Map<String, String> row, MappingField mappingField) {
        // (32.71777362108976000,-96.80840102118572000)
        String longitude = null;

        try {

            String text = row.get("Lat_Long Location");

            // no coords equivalent
            if (text.equals("(,)"))
                return null;

            String coordinates = text.replaceAll("[()]", "");
            longitude = coordinates.split(",")[1];

        } catch (Exception e) {
            log.error("[LONGITUDE_CSV_311_DALLAS] " + e.getMessage());
        }

        return longitude;
    }

    // 05/11/2023 07:56:33 AM
    public static String REPORTED_AT_CSV_311_DALLAS(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Created Date");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    public static String LATITUDE_CSV_CRIME_DALLAS(Map<String, String> row, MappingField mappingField) {
        // "7152 FAIR OAKS AVE DALLAS, TX 75231 (32.87309, -96.75785)"
        String latitude = null;

        try {

            String text = row.get("Location1");

            Pattern pattern = Pattern.compile("\\(([^,]+),\\s*([^\\)]+)\\)");

            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                latitude = matcher.group(1);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return latitude;
    }

    public static String LONGITUDE_CSV_CRIME_DALLAS(Map<String, String> row, MappingField mappingField) {
        // "7152 FAIR OAKS AVE DALLAS, TX 75231 (32.87309, -96.75785)"
        String longitude = null;

        try {

            String text = row.get("Location1");
            Pattern pattern = Pattern.compile("\\(([^,]+),\\s*([^\\)]+)\\)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                longitude = matcher.group(2);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return longitude;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public static String REPORTED_AT_CSV_CRIME_DALLAS(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Date of Report");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public static String REPORTED_AT2_CSV_CRIME_DALLAS(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Date1 of Occurrence");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public static String REPORTED_AT_CRIME_DALLAS(JsonNode item, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = item.at("/reporteddate").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public static String REPORTED_AT2_CRIME_DALLAS(JsonNode item, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = item.at("/date1").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

}
