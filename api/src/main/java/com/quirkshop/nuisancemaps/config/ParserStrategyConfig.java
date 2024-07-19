package com.quirkshop.nuisancemaps.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserStrategyConfig {

    @Bean
    public Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions() {

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.LATITUDE_311_DALLAS, this::LATITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_311_DALLAS, this::LONGITUDE_311_DALLAS);

        return parsingFunctions;
    }

    public String LATITUDE_311_DALLAS(JsonNode item) {
        // {..., "lat_location": (32.77937339624264000,-96.85251201839743000), ...}
        String latitude = item.at("/lat_location").asText();
        return latitude;
    }

    public String LONGITUDE_311_DALLAS(JsonNode item) {
        // {..., "lat_location": (32.77937339624264000,-96.85251201839743000), ...}
        String longitude = item.at("/lat_location").asText();
        return longitude;
    }
}
