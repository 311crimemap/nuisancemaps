package com.quirkshop.nuisancemaps.config;

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
    public Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions() {

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.LATITUDE_311_DALLAS, this::LATITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_311_DALLAS, this::LONGITUDE_311_DALLAS);

        return parsingFunctions;
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
}
