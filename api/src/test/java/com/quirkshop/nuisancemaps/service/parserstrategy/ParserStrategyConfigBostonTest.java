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
public class ParserStrategyConfigBostonTest {
    @Test
    @Transactional
    public void REPORTEDAT_BOSTON_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_BOSTON).isNotNull();

        Map<String, String> row = Map.of("OCCURRED_ON_DATE", "2020-12-31 20:30:00");

        String value = ParserStrategyConfigBoston.REPORTED_AT_CSV_CRIME_BOSTON(row);
        assertThat(value).isEqualTo("2020-12-31T20:30:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTEDAT_BOSTON_TIMEZONE_OFFSET_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON).isNotNull();

        Map<String, String> row = Map.of("OCCURRED_ON_DATE", "2020-12-31 20:30:00+00");

        String value = ParserStrategyConfigBoston.REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON(row);
        assertThat(value).isEqualTo("2020-12-31T20:30:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

}
