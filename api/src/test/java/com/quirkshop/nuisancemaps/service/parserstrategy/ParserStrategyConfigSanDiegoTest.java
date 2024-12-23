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
public class ParserStrategyConfigSanDiegoTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_SAN_DIEGO_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_SAN_DIEGO).isNotNull();

        Map<String, String> row = Map.of("occured_on", "2022-02-11 22:00:00");

        String value = ParserStrategyConfigSanDiego.REPORTED_AT_CSV_CRIME_SAN_DIEGO(row, null);
        assertThat(value).isEqualTo("2022-02-11T22:00:00");
    }

}
