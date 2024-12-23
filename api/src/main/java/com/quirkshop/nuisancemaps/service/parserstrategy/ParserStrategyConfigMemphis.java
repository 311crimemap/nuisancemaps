package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Memphis
 */

public class ParserStrategyConfigMemphis {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 05/11/2023 07:56:33 AM
    public static String REPORTED_AT_CSV_CRIME_MEMPHIS(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Offense Date");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 01/05/2016 09:11:11 PM
    public static String REPORTED_AT_CSV_311_MEMPHIS(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("REPORTED_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    public static String LATITIUDE_CSV_311_MEMPHIS(Map<String, String> row, MappingField mappingField) {
        // POINT (-90.04925 35.14976)
        String longitude = null;

        try {
            String text = row.get("location1");

            if (text.isEmpty())
                return null;

            String coordinates = text
                    .replaceAll("POINT", "")
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "")
                    .trim();

            longitude = coordinates.split("\\s+")[1];

        } catch (Exception e) {
            log.error("[LATITUDE_CSV_311_MEMPHIS] " + e.getMessage());
        }

        return longitude;
    }

    public static String LONGITUDE_CSV_311_MEMPHIS(Map<String, String> row, MappingField mappingField) {
        // POINT (-90.04925 35.14976)
        String longitude = null;

        try {
            String text = row.get("location1");

            if (text.isEmpty())
                return null;

            String coordinates = text
                    .replaceAll("POINT", "")
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "")
                    .trim();

            longitude = coordinates.split("\\s+")[0];

        } catch (Exception e) {
            log.error("[LONGITUDE_CSV_311_MEMPHIS] " + e.getMessage());
        }

        return longitude;
    }

}
