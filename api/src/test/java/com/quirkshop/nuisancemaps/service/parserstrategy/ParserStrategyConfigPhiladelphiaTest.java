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
public class ParserStrategyConfigPhiladelphiaTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_PHILADELPHIA_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_PHILADELPHIA).isNotNull();

        Map<String, String> row = Map.of("dispatch_date_time", "2024-07-01 22:59:00+00");

        String value = ParserStrategyConfigPhiladelphia.REPORTED_AT_CSV_CRIME_PHILADELPHIA(row);
        assertThat(value).isEqualTo("2024-07-01T22:59:00");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_311_PHILADELPHIA_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_311_PHILADELPHIA).isNotNull();

        Map<String, String> row = Map.of("requested_datetime", "2024-01-05 17:23:40+00");

        String value = ParserStrategyConfigPhiladelphia.REPORTED_AT_CSV_311_PHILADELPHIA(row);
        assertThat(value).isEqualTo("2024-01-05T17:23:40");
    }

}
