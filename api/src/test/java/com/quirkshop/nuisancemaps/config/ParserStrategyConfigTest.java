package com.quirkshop.nuisancemaps.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigTest {

    @Autowired
    private ResourceLoader resourceLoader;

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

    @Test
    @Transactional
    public void CREATED_DATE_311_NEWYORKCITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.CREATED_DATE_311_NEWYORKCITY).isNotNull();

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = parserStrategyConfig
                .parsingFunctionsMap();

        Map<String, String> row = Map.of("Created Date", "02/11/2024 10:39:24 PM");

        String value = parserStrategyConfig.CREATED_DATE_311_NEWYORKCITY(row);
        assertThat(value).isEqualTo("2024-02-11T22:39:24");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTEDAT_BOSTON_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTEDAT_BOSTON).isNotNull();

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = parserStrategyConfig
                .parsingFunctionsMap();

        Map<String, String> row = Map.of("OCCURRED_ON_DATE", "2020-12-31 20:30:00");

        String value = parserStrategyConfig.REPORTEDAT_BOSTON(row);
        assertThat(value).isEqualTo("2020-12-31T20:30:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTEDAT_BOSTON_TIMEZONE_OFFSET_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTEDAT_BOSTON_TIMEZONE_OFFSET).isNotNull();

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = parserStrategyConfig
                .parsingFunctionsMap();

        Map<String, String> row = Map.of("OCCURRED_ON_DATE", "2020-12-31 20:30:00+00");

        String value = parserStrategyConfig.REPORTEDAT_BOSTON_TIMEZONE_OFFSET(row);
        assertThat(value).isEqualTo("2020-12-31T20:30:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void STREET_NAME_ERSI_AUSTIN_TEST() throws JsonMappingException, JsonProcessingException, IOException {
        assertThat(ParserStrategy.STREET_NAME_ERSI_AUSTIN).isNotNull();

        Resource jsonResource = resourceLoader.getResource("classpath:data/ersi-2024-07-01-2024-08-15-atx.json");
        InputStream inputstream = jsonResource.getInputStream();

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions =
            parserStrategyConfig.parsingFunctionsJSON();

        JsonNode items = objectMapper.readTree(inputstream);
        //System.out.println(items);
        JsonNode item = items.at("/features/2");
        String value = parserStrategyConfig.STREET_NAME_ERSI_AUSTIN(item);
        assertThat(value).isEqualTo("8800 NORTH PLZ");
    }

    @Test
    @Transactional
    public void OCCURRENCE_DATE_ERSI_AUSTIN_TEST() throws JsonMappingException, JsonProcessingException, IOException {
        assertThat(ParserStrategy.STREET_NAME_ERSI_AUSTIN).isNotNull();

        Resource jsonResource = resourceLoader.getResource("classpath:data/ersi-2024-07-01-2024-08-15-atx.json");
        InputStream inputstream = jsonResource.getInputStream();

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = parserStrategyConfig.parsingFunctionsJSON();

        JsonNode items = objectMapper.readTree(inputstream);
        // System.out.println(items);
        JsonNode item = items.at("/features/2");

        //date: 1705276800000
        //time: 1136
        String value = parserStrategyConfig.OCCURRENCE_DATE_ERSI_AUSTIN(item);
        assertThat(value).isEqualTo("2024-01-15T11:36:00");
    }

}
