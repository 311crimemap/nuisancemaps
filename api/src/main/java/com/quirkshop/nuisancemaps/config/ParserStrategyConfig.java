package com.quirkshop.nuisancemaps.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

        return parsingFunctions;
    }

    @Bean
    public Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctionsMap() {

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.REPORTEDAT_CRIME_NEWYORKCITY,
                this::REPORTEDAT_CRIME_NEWYORKCITY);

        return parsingFunctions;
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
}
