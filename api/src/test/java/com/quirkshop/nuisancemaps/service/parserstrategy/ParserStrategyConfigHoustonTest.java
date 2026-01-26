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
public class ParserStrategyConfigHoustonTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void REPORTED_AT_CSV2_311_HOUSTON_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV2_311_HOUSTON).isNotNull();

        Map<String, String> row = Map.of("SR CREATE DATE", "2021-01-08 00:45:40");

        String value = ParserStrategyConfigHouston.REPORTED_AT_CSV2_311_HOUSTON(row, null);
        assertThat(value).isEqualTo("2021-01-08T00:45:40");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_HOUSTON_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_HOUSTON).isNotNull();

        Map<String, String> row = Map.of(
                "Occurrence Date", "2025-01-01",
                "Occurrence Hour", "0");

        Map<String, String> row2 = Map.of(
                "Occurrence Date", "2025-11-15",
                "Occurrence Hour", "21");

        String value = ParserStrategyConfigHouston.REPORTED_AT_CSV_CRIME_HOUSTON(row, null);
        assertThat(value).isEqualTo("2025-01-01T00:00:00");

        String value2 = ParserStrategyConfigHouston.REPORTED_AT_CSV_CRIME_HOUSTON(row2, null);
        assertThat(value2).isEqualTo("2025-11-15T21:00:00");
    }

    @Test
    @Transactional
    public void ADDRESSS_CSV_CRIME_HOUSTON_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.ADDRESS_CSV_CRIME_HOUSTON).isNotNull();

        Map<String, String> row = Map.of(
                "Street Number", "4722",
                "Street Name", "OLD SPANISH",
                "Street Type", "",
                "Street Suffix", "",
                "City", "HOUSTON",
                "ZIP Code", "706255");

        Map<String, String> row2 = Map.of(
                "Street Number", "4722",
                "Street Name", "OLD SPANISH",
                "Street Type", "TRL",
                "Street Suffix", "N",
                "City", "HOUSTON",
                "ZIP Code", "706255");

        String value = ParserStrategyConfigHouston.ADDRESS_CSV_CRIME_HOUSTON(row, null);
        assertThat(value).isEqualTo("4722 OLD SPANISH HOUSTON 706255");

        String value2 = ParserStrategyConfigHouston.ADDRESS_CSV_CRIME_HOUSTON(row2, null);
        assertThat(value2).isEqualTo("4722 N OLD SPANISH TRL HOUSTON 706255");

    }
}
