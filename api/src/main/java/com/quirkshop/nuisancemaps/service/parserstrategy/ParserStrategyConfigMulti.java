package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Multi
 */

public class ParserStrategyConfigMulti {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 05/11/2023 07:56:33 AM
    // 01/09/2022 01:18:38 AM
    public static String REPORTED_AT_CSV_MMddyyyyhhmmssa(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 2019/07/12 19:04:00+00
    // slash and + timezone
    public static String REPORTED_AT_CSV_yyyyMMddHHmmssx_SLASH_TZ(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssx");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 5/4/2022 5:54:30 PM
    public static String REPORTED_AT_CSV_Mdyyyyhhmmssa_SLASH(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 02/01/2024
    public static String REPORTED_AT_CSV_MMddyyyy_SLASH(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    /* POINT */

    // POINT (-94.44111 39.04642)
    public static String LATITUDE_CSV_POINT_MULTI(Map<String, String> row, MappingField mappingField) {
        String latitude = null;

        try {
            String field = mappingField.getField();
            String location = row.get(field);
            if (location != null && location.contains("POINT")) {
                String[] coords = location.replace("POINT (", "").replace(")", "").split(" ");
                latitude = coords[1];
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return latitude;
    }

    // POINT (-94.44111 39.04642)
    public static String LONGITUDE_CSV_POINT_MULTI(Map<String, String> row, MappingField mappingField) {
        String longitude = null;

        try {
            String field = mappingField.getField();
            String location = row.get(field);
            if (location != null && location.contains("POINT")) {
                String[] coords = location.replace("POINT (", "").replace(")", "").split(" ");
                longitude = coords[0];
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return longitude;
    }

    // (37.7348199929233°, -122.2006649756992°)
    public static String LATITUDE_CSV_COORDS_DEGREE_MULTI(Map<String, String> row, MappingField mappingField) {
        return _CSV_COORDS_DEGREE_MULTI(row, mappingField, 1);
    }

    // (37.7348199929233°, -122.2006649756992°)
    public static String LONGITUDE_CSV_COORDS_DEGREE_MULTI(Map<String, String> row, MappingField mappingField) {
        return _CSV_COORDS_DEGREE_MULTI(row, mappingField, 2);
    }

    public static String _CSV_COORDS_DEGREE_MULTI(Map<String, String> row, MappingField mappingField, int coordIndex) {
        String coord = null;

        try {
            String field = mappingField.getField();
            String location = row.get(field);

            if (location.isEmpty())
                return null;

            Pattern pattern = Pattern.compile("\\(([-+]?[0-9]*\\.?[0-9]+)°?,\\s*(-?\\d*\\.?\\d+)°?\\)");
            Matcher matcher = pattern.matcher(location);

            if (matcher.find()) {
                coord = matcher.group(coordIndex);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return coord;
    }

}
