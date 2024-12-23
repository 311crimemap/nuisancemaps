package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

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
public class ParserStrategyConfigAustinTest {

    @Autowired
    private ResourceLoader resourceLoader;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void STREET_NAME_ERSI_AUSTIN_TEST() throws JsonMappingException, JsonProcessingException, IOException {
        assertThat(ParserStrategy.STREET_NAME_ERSI_AUSTIN).isNotNull();

        Resource jsonResource = resourceLoader.getResource("classpath:data/ersi-2024-07-01-2024-08-15-atx.json");
        InputStream inputstream = jsonResource.getInputStream();

        JsonNode items = objectMapper.readTree(inputstream);
        // System.out.println(items);
        JsonNode item = items.at("/features/2");
        String value = ParserStrategyConfigAustin.STREET_NAME_ERSI_AUSTIN(item, null);
        assertThat(value).isEqualTo("8800 NORTH PLZ");
    }

    @Test
    @Transactional
    public void OCCURRENCE_DATE_ERSI_AUSTIN_TEST() throws JsonMappingException, JsonProcessingException, IOException {
        assertThat(ParserStrategy.STREET_NAME_ERSI_AUSTIN).isNotNull();

        Resource jsonResource = resourceLoader.getResource("classpath:data/ersi-2024-07-01-2024-08-15-atx.json");
        InputStream inputstream = jsonResource.getInputStream();

        JsonNode items = objectMapper.readTree(inputstream);
        // System.out.println(items);
        JsonNode item = items.at("/features/2");

        // date: 1705276800000
        // time: 1136
        String value = ParserStrategyConfigAustin.OCCURRENCE_DATE_ERSI_AUSTIN(item, null);
        assertThat(value).isEqualTo("2024-01-15T11:36:00");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_AUSTIN_TEST() {
        assertThat(ParserStrategy.REPORTED_AT_CSV_AUSTIN).isNotNull();

        Map<String, String> row = Map.of("Occurred Date Time", "09/21/2023 07:18:00 AM");

        String value = ParserStrategyConfigAustin.REPORTED_AT_CSV_AUSTIN(row, null);
        assertThat(value).isEqualTo("2023-09-21T07:18:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTED_AT2_CSV_AUSTIN_TEST() {
        assertThat(ParserStrategy.REPORTED_AT2_CSV_AUSTIN).isNotNull();

        Map<String, String> row = Map.of("Report Date Time", "04/15/2016 01:09:00 PM");

        String value = ParserStrategyConfigAustin.REPORTED_AT2_CSV_AUSTIN(row, null);
        assertThat(value).isEqualTo("2016-04-15T13:09:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

}
