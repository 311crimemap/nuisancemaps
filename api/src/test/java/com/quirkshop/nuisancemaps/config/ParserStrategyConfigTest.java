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

import java.time.LocalDateTime;
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
        assertThat(ParserStrategy.LATITUDE_311_DALLAS).isNotNull();
        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctionsJSON();
        String jsonStr = "{\"lat_location\": \"(32.77937339624264000,-96.85251201839743000)\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = parserStrategyConfig.LATITUDE_311_DALLAS(item);
        assertThat(value).isEqualTo("32.77937339624264000");
    }

    @Test
    @Transactional
    public void LONGITUDE_311_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_311_DALLAS).isNotNull();

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctionsJSON();
        String jsonStr = "{\"lat_location\": \"(32.77937339624264000,-96.85251201839743000)\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = parserStrategyConfig.LONGITUDE_311_DALLAS(item);
        assertThat(value).isEqualTo("-96.85251201839743000");
    }

    @Test
    @Transactional
    public void REPORTEDAT_CRIME_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTEDAT_CRIME_DALLAS).isNotNull();

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctionsJSON();
        String jsonStr = "{\"reporteddate\":\"2016-07-19 17:22:00.0000000\",\"date1\":\"2016-07-19 00:00:00.0000000\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = parserStrategyConfig.REPORTEDAT_CRIME_DALLAS(item);
        assertThat(value).isEqualTo("2016-07-19T17:22:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTEDAT2_CRIME_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTEDAT2_CRIME_DALLAS).isNotNull();

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctionsJSON();
        String jsonStr = "{\"reporteddate\":\"2016-07-19 17:22:00.0000000\",\"date1\":\"2016-07-19 00:00:00.0000000\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = parserStrategyConfig.REPORTEDAT2_CRIME_DALLAS(item);
        assertThat(value).isEqualTo("2016-07-19T00:00:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTEDAT_CRIME_NEWYORKCITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTEDAT_CRIME_NEWYORKCITY).isNotNull();

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = parserStrategyConfig
                .parsingFunctionsMap();

        Map<String, String> row = Map.of("RPT_DT", "05/01/2024");

        String value = parserStrategyConfig.REPORTEDAT_CRIME_NEWYORKCITY(row);
        assertThat(value).isEqualTo("2024-05-01T00:00:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

}
