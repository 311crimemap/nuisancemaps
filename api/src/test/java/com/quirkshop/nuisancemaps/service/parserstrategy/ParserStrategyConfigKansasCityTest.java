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
public class ParserStrategyConfigKansasCityTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void ADDRESS_CSV_CRIME_KANSAS_CITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.ADDRESS_CSV_CRIME_KANSAS_CITY).isNotNull();

        Map<String, String> row = Map.of("Address", "0  MEMORIAL DR",
                "City", "KANSAS CITY",
                "Zip Code", "64108");

        String value = ParserStrategyConfigKansasCity.ADDRESS_CSV_CRIME_KANSAS_CITY(row, null);
        assertThat(value).isEqualTo("0  MEMORIAL DR KANSAS CITY MO 64108");
    }

    @Test
    @Transactional
    public void LATITUDE_CSV_CRIME_KANSAS_CITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LATITUDE_CSV_CRIME_KANSAS_CITY).isNotNull();

        Map<String, String> row = Map.of("Location", "POINT (-94.44111 39.04642)");

        String value = ParserStrategyConfigKansasCity.LATITUDE_CSV_CRIME_KANSAS_CITY(row, null);
        assertThat(value).isEqualTo("39.04642");
    }

    @Test
    @Transactional
    public void LONGITUDE_CSV_CRIME_KANSAS_CITY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_CSV_CRIME_KANSAS_CITY).isNotNull();

        Map<String, String> row = Map.of("Location", "POINT (-94.44111 39.04642)");

        String value = ParserStrategyConfigKansasCity.LONGITUDE_CSV_CRIME_KANSAS_CITY(row, null);
        assertThat(value).isEqualTo("-94.44111");
    }

}
