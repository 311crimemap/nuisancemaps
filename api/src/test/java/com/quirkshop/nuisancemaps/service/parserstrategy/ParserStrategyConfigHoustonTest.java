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

        String value = ParserStrategyConfigHouston.REPORTED_AT_CSV2_311_HOUSTON(row);
        assertThat(value).isEqualTo("2021-01-08T00:45:40");
    }

}
