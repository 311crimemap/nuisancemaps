package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigNewYorkCityTest {

    @Test
    @Transactional
    public void REPORTEDAT_CRIME_NEWYORKCITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_NEW_YORK_CITY).isNotNull();

        Map<String, String> row = Map.of("RPT_DT", "05/01/2024");

        String value = ParserStrategyConfigNewYorkCity.REPORTED_AT_CSV_CRIME_NEW_YORK_CITY(row);
        assertThat(value).isEqualTo("2024-05-01T00:00:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void CREATED_DATE_311_NEWYORKCITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_311_NEW_YORK_CITY).isNotNull();

        Map<String, String> row = Map.of("Created Date", "02/11/2024 10:39:24 PM");

        String value = ParserStrategyConfigNewYorkCity.REPORTED_AT_CSV_311_NEW_YORK_CITY(row);
        assertThat(value).isEqualTo("2024-02-11T22:39:24");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }


}
