package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigSeattleTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void LATITUDE_CRIME_SEATTLE_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LATITUDE_JSON_CRIME_SEATTLE).isNotNull();

        String jsonStr = "{\"latitude\": \"32.77937339624264000\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = ParserStrategyConfigSeattle.LATITUDE_JSON_CRIME_SEATTLE(item, null);
        assertThat(value).isEqualTo("32.77937339624264000");

        // REDACTED
        jsonStr = "{\"latitude\": \"REDACTED\"}";
        item = objectMapper.readTree(jsonStr);

        value = ParserStrategyConfigSeattle.LATITUDE_JSON_CRIME_SEATTLE(item, null);
        assertThat(value).isEqualTo(null);

    }

    @Test
    @Transactional
    public void LONGITUDE_CRIME_SEATTLE_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_JSON_CRIME_SEATTLE).isNotNull();

        String jsonStr = "{\"longitude\": \"-96.85251201839743000\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = ParserStrategyConfigSeattle.LONGITUDE_JSON_CRIME_SEATTLE(item, null);
        assertThat(value).isEqualTo("-96.85251201839743000");

        // REDACTED
        jsonStr = "{\"longitude\": \"REDACTED\"}";
        item = objectMapper.readTree(jsonStr);

        value = ParserStrategyConfigSeattle.LONGITUDE_JSON_CRIME_SEATTLE(item, null);
        assertThat(value).isEqualTo(null);

    }

}
