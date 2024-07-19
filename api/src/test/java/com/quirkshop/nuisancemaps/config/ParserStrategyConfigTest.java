package com.quirkshop.nuisancemaps.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigTest {

    @Autowired
    private ParserStrategyConfig parserStrategyConfig;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void LATITUDE_311_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctions();
        String jsonStr = "{\"lat_location\": \"(32.77937339624264000,-96.85251201839743000)\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = parserStrategyConfig.LATITUDE_311_DALLAS(item);
        assertThat(value).isEqualTo("32.77937339624264000");
    }

    @Test
    @Transactional
    public void LONGITUDE_311_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctions();
        String jsonStr = "{\"lat_location\": \"(32.77937339624264000,-96.85251201839743000)\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = parserStrategyConfig.LONGITUDE_311_DALLAS(item);
        assertThat(value).isEqualTo("-96.85251201839743000");
    }

}
