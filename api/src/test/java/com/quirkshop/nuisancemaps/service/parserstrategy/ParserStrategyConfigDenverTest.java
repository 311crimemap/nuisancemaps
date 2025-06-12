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
public class ParserStrategyConfigDenverTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    // 10/14/2020 10:20:00 PM
    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_DENVER_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_DENVER).isNotNull();

        Map<String, String> row = Map.of("REPORTED_DATE", "10/14/2020 10:20:00 PM");

        String value = ParserStrategyConfigDenver.REPORTED_AT_CSV_CRIME_DENVER(row, null);
        assertThat(value).isEqualTo("2020-10-14T22:20:00");
    }

    // 12/31/2023 10:40:02 AM
    @Test
    @Transactional
    public void REPORTED_AT_CSV_311_DENVER_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_311_DENVER).isNotNull();

        Map<String, String> row = Map.of("Case Created dttm", "12/6/2023 10:40:02 AM");

        String value = ParserStrategyConfigDenver.REPORTED_AT_CSV_311_DENVER(row, null);
        assertThat(value).isEqualTo("2023-12-06T10:40:02");
    }

}
