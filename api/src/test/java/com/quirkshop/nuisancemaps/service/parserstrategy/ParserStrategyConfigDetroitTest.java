package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigDetroitTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    // 2024/12/16 00:25:00+00
    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_DETROIT_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_DETROIT).isNotNull();

        Map<String, String> row = Map.of("incident_occurred_at", "2024/12/16 00:25:00+00");

        String value = ParserStrategyConfigDetroit.REPORTED_AT_CSV_CRIME_DETROIT(row, null);
        assertThat(value).isEqualTo("2024-12-16T00:25:00");
    }

    // 2024/11/25 12:34:56+00
    @Test
    @Transactional
    public void REPORTED_AT_CSV_311_DETROIT_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_311_DETROIT).isNotNull();

        Map<String, String> row = Map.of("Created_At", "2024/11/25 12:34:56+00");

        String value = ParserStrategyConfigDetroit.REPORTED_AT_CSV_311_DETROIT(row, null);
        assertThat(value).isEqualTo("2024-11-25T12:34:56");
    }

}
