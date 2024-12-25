package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    // 2024-01-01 05:04:51.0
    // 2024-04-18 16:52:05.19
    // 2024-04-18 16:52:05.197
    // 2024-04-18 16:52:05.1974
    // strips any variable ms after '.'
    public static String REPORTED_AT_CSV_yyyyMMddHHmmssSSS_DASH(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);
            if (text == null)
                return null;

            int msIndex = text.indexOf(".");
            if (msIndex != -1) {
                text = text.substring(0, msIndex); // Remove ms part after '.'
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

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

    // 1/3/2024
    public static String REPORTED_AT_CSV_Mdyyyy_SLASH(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    /*
     * POINT
     */

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

    /*
     * Convert Web Mercator (EPSG:3857) to WGS 84 (EPSG:4326) / spatialRefId 4326,
     * "X / Y" or "SRX / SRY" values to lat lng
     * NB: X -> longitude, Y -> latitude
     */

    // X: -10045681.646
    public static String LONGITUDE_CSV_EPSG_3857_TO_4326_MULTI(Map<String, String> row, MappingField mappingField) {
        final double EARTH_RADIUS = 6378137.0;

        try {
            String field = mappingField.getField();
            String X = row.get(field);

            double x = Double.parseDouble(X);
            double longitudeRadians = x / EARTH_RADIUS;

            // Convert radians to degrees
            Double longitudeDegrees = Math.toDegrees(longitudeRadians);
            return longitudeDegrees.toString();

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    // Y: 4680864.002
    public static String LATITUDE_CSV_EPSG_3857_TO_4326_MULTI(Map<String, String> row, MappingField mappingField) {
        final double EARTH_RADIUS = 6378137.0;

        try {
            String field = mappingField.getField();
            String Y = row.get(field);
            double y = Double.parseDouble(Y);

            // Convert to latitude in radians using the arctan of sinh(y / R)
            double latitudeRadians = Math.atan(Math.sinh(y / EARTH_RADIUS));

            // Convert radians to degrees
            Double latitudeDegrees = Math.toDegrees(latitudeRadians);
            return latitudeDegrees.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

}
