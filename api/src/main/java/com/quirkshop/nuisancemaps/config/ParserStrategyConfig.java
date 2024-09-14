package com.quirkshop.nuisancemaps.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quirkshop.nuisancemaps.WorkerApplication;

@Configuration
public class ParserStrategyConfig {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Bean
    public Map<ParserStrategy, Function<JsonNode, String>> parsingFunctionsJSON() {

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.LATITUDE_311_DALLAS, this::LATITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_311_DALLAS, this::LONGITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_CRIME_DALLAS, this::REPORTEDAT_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT2_CRIME_DALLAS, this::REPORTEDAT2_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.STREET_NAME_ERSI_AUSTIN, this::STREET_NAME_ERSI_AUSTIN);
        parsingFunctions.put(ParserStrategy.OCCURRENCE_DATE_ERSI_AUSTIN, this::OCCURRENCE_DATE_ERSI_AUSTIN);

        return parsingFunctions;
    }

    @Bean
    public Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctionsMap() {

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.REPORTEDAT_BOSTON, this::REPORTEDAT_BOSTON);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_BOSTON_TIMEZONE_OFFSET,
                this::REPORTEDAT_BOSTON_TIMEZONE_OFFSET);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_CRIME_NEWYORKCITY,
                this::REPORTEDAT_CRIME_NEWYORKCITY);
        parsingFunctions.put(ParserStrategy.CREATED_DATE_311_NEWYORKCITY, this::CREATED_DATE_311_NEWYORKCITY);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_AUSTIN, this::REPORTED_AT_CSV_AUSTIN);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_AUSTIN, this::REPORTED_AT2_CSV_AUSTIN);
        parsingFunctions.put(ParserStrategy.LATITUDE_CSV_CRIME_DALLAS, this::LATITUDE_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_CSV_CRIME_DALLAS, this::LONGITUDE_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_CSV_CRIME_DALLAS, this::REPORTEDAT_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT2_CSV_CRIME_DALLAS, this::REPORTEDAT2_CSV_CRIME_DALLAS);

        return parsingFunctions;
    }

    // LocalDateTime.parse requires ISO format but field is a simple date with 24 hr
    // time
    // (MM-DD-YYYY HH:mm:ss) 2020-12-31 20:30:00
    public String REPORTEDAT_BOSTON(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("OCCURRED_ON_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse requires ISO format but field is a simple date with 24 hr
    // time and timezone offset: 2020-12-31 20:30:00+00
    public String REPORTEDAT_BOSTON_TIMEZONE_OFFSET(Map<String, String> row) {
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
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse requires ISO format but field is a simple date
    // (MM/DD/YYYY) - only for CSV (but not JSON)
    public String REPORTEDAT_CRIME_NEWYORKCITY(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("RPT_DT");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle hh:mm:ss am/pm marker
    public String CREATED_DATE_311_NEWYORKCITY(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Created Date");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    public String LATITUDE_311_DALLAS(JsonNode item) {
        // {..., "lat_location": (32.77937339624264000,-96.85251201839743000), ...}
        String latitude = null;

        try {
            String text = item.at("/lat_location").asText();
            String coordinates = text.replaceAll("[()]", "");
            latitude = coordinates.split(",")[0];
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return latitude;
    }

    public String LONGITUDE_311_DALLAS(JsonNode item) {
        // {..., "lat_location": (32.77937339624264000,-96.85251201839743000), ...}
        String longitude = null;

        try {
            String text = item.at("/lat_location").asText();
            String coordinates = text.replaceAll("[()]", "");
            longitude = coordinates.split(",")[1];
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return longitude;
    }

    public String LATITUDE_CSV_CRIME_DALLAS(Map<String, String> row) {
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
            log.info(e.getMessage());
        }

        return latitude;
    }

    public String LONGITUDE_CSV_CRIME_DALLAS(Map<String, String> row) {
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
            log.info(e.getMessage());
        }

        return longitude;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public String REPORTEDAT_CSV_CRIME_DALLAS(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Date of Report");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public String REPORTEDAT2_CSV_CRIME_DALLAS(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Date1 of Occurrence");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public String REPORTEDAT_CRIME_DALLAS(JsonNode item) {
        String dateStr = null;
        try {
            String text = item.at("/reporteddate").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    public String REPORTEDAT2_CRIME_DALLAS(JsonNode item) {
        String dateStr = null;
        try {
            String text = item.at("/date1").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return dateStr;
    }

    public String STREET_NAME_ERSI_AUSTIN(JsonNode item) {
        String value = null;

        try {
            String addressBlock = item.at("/attributes/ADDRESS_BLOCK").asText();
            String streetName = item.at("/attributes/STREET_NAME").asText();
            String streetType = item.at("/attributes/STREET_TYPE").asText();
            value = String.join(" ", addressBlock, streetName, streetType);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return value;
    }

    public String OCCURRENCE_DATE_ERSI_AUSTIN(JsonNode item) {
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
            log.info(e.getMessage());
        }

        return dateStr;
    }

    // '09/21/2023 07:18:00 AM'
    public String REPORTED_AT_CSV_AUSTIN(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Occurred Date Time");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.info("REPORTED_AT_CSV_AUSTIN: " + row.get("Occurred Date Time") + " | " + e.getMessage());
        }

        return dateStr;
    }

    // '09/21/2023 07:18:00 AM'
    public String REPORTED_AT2_CSV_AUSTIN(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Report Date Time");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.info("REPORTED_AT2_CSV_AUSTIN: " + row.get("Report Date Time") + " | " + e.getMessage());
        }

        return dateStr;
    }

}
