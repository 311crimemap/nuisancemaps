package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.stereotype.Component;

import com.quirkshop.nuisancemaps.WorkerApplication;

@Component
public class ParserStrategyConfig {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Bean
    public Map<ParserStrategy, Function<JsonNode, String>> parsingFunctionsJSON() {

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.LATITUDE_311_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_311_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTEDAT_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT2_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTEDAT2_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.STREET_NAME_ERSI_AUSTIN,
                ParserStrategyConfigAustin::STREET_NAME_ERSI_AUSTIN);
        parsingFunctions.put(ParserStrategy.OCCURRENCE_DATE_ERSI_AUSTIN,
                ParserStrategyConfigAustin::OCCURRENCE_DATE_ERSI_AUSTIN);

        return parsingFunctions;
    }

    @Bean
    public Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctionsMap() {

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = new HashMap<>();

        // austin
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_AUSTIN,
                ParserStrategyConfigAustin::REPORTED_AT_CSV_AUSTIN);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_AUSTIN,
                ParserStrategyConfigAustin::REPORTED_AT2_CSV_AUSTIN);
        parsingFunctions.put(ParserStrategy.CREATED_DATE_CSV_AUSTIN,
                ParserStrategyConfigAustin::CREATED_DATE_CSV_AUSTIN);

        // dallas
        parsingFunctions.put(ParserStrategy.LATITUDE_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.LATITUDE_CSV_311_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_CSV_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_CSV_311_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_CSV_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_DALLAS,
                ParserStrategyConfigDallas::REPORTED_AT_CSV_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTEDAT_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT2_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTEDAT2_CSV_CRIME_DALLAS);

        // chicago
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_CHICAGO,
                this::REPORTED_AT_CSV_CRIME_CHICAGO);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_CHICAGO,
                this::REPORTED_AT_CSV_311_CHICAGO);

        // san francisco
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_SAN_FRANCISCO,
                this::REPORTED_AT_CSV_CRIME_SAN_FRANCISCO);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_SAN_FRANCISCO,
                this::REPORTED_AT_CSV_311_SAN_FRANCISCO);

        // new york city
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_NEW_YORK_CITY,
                this::REPORTED_AT_CSV_CRIME_NEW_YORK_CITY);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY,
                this::REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY);

        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_NEW_YORK_CITY,
                this::REPORTED_AT_CSV_311_NEW_YORK_CITY);

        // boston
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON,
                this::REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_BOSTON, this::REPORTED_AT_CSV_CRIME_BOSTON);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_BOSTON, this::REPORTED_AT_CSV_311_BOSTON);

        return parsingFunctions;
    }
    /*
     * chicago
     */

    // 09/11/2024 12:00:00 AM
    public String REPORTED_AT_CSV_CRIME_CHICAGO(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Date");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 09/19/2024 08:14:37 AM
    public String REPORTED_AT_CSV_311_CHICAGO(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("CREATED_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
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
     * san francisco
     */

    // 2023/03/16 10:15:00 PM
    public String REPORTED_AT_CSV_CRIME_SAN_FRANCISCO(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Incident Datetime");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_CRIME_SAN_FRANCISCO] " + e.getMessage());
        }

        return dateStr;
    }

    // 06/09/2021 08:36:00 AM
    public String REPORTED_AT_CSV_311_SAN_FRANCISCO(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Opened");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_311_SAN_FRANCISCO] " + e.getMessage());
        }

        return dateStr;
    }

    /*
     * new york city
     */

    // LocalDateTime.parse requires ISO format but field is a simple date
    // (MM/DD/YYYY) - only for CSV (but not JSON)
    public String REPORTED_AT_CSV_CRIME_NEW_YORK_CITY(Map<String, String> row) {
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

    public String REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY(Map<String, String> row) {
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
    public String REPORTED_AT_CSV_311_NEW_YORK_CITY(Map<String, String> row) {
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

    /*
     * BOSTON
     */

    // LocalDateTime.parse requires ISO format but field is a simple date with 24 hr
    // time and timezone offset:
    // 2020-12-31 20:30:00+00
    public String REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON(Map<String, String> row) {
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
    public String REPORTED_AT_CSV_CRIME_BOSTON(Map<String, String> row) {
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
    public String REPORTED_AT_CSV_311_BOSTON(Map<String, String> row) {
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
