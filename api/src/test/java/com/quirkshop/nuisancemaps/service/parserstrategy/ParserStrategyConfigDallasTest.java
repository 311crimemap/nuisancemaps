package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigDallasTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void LATITUDE_311_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LATITUDE_311_DALLAS).isNotNull();

        String jsonStr = "{\"lat_location\": \"(32.77937339624264000,-96.85251201839743000)\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = ParserStrategyConfigDallas.LATITUDE_311_DALLAS(item);
        assertThat(value).isEqualTo("32.77937339624264000");
    }

    @Test
    @Transactional
    public void LONGITUDE_311_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_311_DALLAS).isNotNull();

        String jsonStr = "{\"lat_location\": \"(32.77937339624264000,-96.85251201839743000)\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = ParserStrategyConfigDallas.LONGITUDE_311_DALLAS(item);
        assertThat(value).isEqualTo("-96.85251201839743000");
    }

    @Test
    @Transactional
    public void LATITUDE_CSV_CRIME_DALLAS_TEST() {
        assertThat(ParserStrategy.LATITUDE_CSV_CRIME_DALLAS).isNotNull();

        Map<String, String> row = Map.of("Location1", "7152 FAIR OAKS AVE DALLAS, TX 75231 (32.87309, -96.75785)");

        String value = ParserStrategyConfigDallas.LATITUDE_CSV_CRIME_DALLAS(row);
        assertThat(value).isEqualTo("32.87309");
    }

    @Test
    @Transactional
    public void LONGITUDE_CSV_CRIME_DALLAS_TEST() {
        assertThat(ParserStrategy.LONGITUDE_CSV_CRIME_DALLAS).isNotNull();

        Map<String, String> row = Map.of("Location1", "7152 FAIR OAKS AVE DALLAS, TX 75231 (32.87309, -96.75785)");

        String value = ParserStrategyConfigDallas.LONGITUDE_CSV_CRIME_DALLAS(row);
        assertThat(value).isEqualTo("-96.75785");
    }

    @Test
    @Transactional
    public void LATITUDE_CSV_311_DALLAS_TEST() {
        assertThat(ParserStrategy.LATITUDE_CSV_311_DALLAS).isNotNull();

        Map<String, String> row = Map.of("Lat_Long Location", "(32.71777362108976000,-96.80840102118572000)");

        String value = ParserStrategyConfigDallas.LATITUDE_CSV_311_DALLAS(row);
        assertThat(value).isEqualTo("32.71777362108976000");
    }

    @Test
    @Transactional
    public void LONGITUDE_CSV_311_DALLAS_TEST() {
        assertThat(ParserStrategy.LONGITUDE_CSV_311_DALLAS).isNotNull();

        Map<String, String> row = Map.of("Lat_Long Location", "(32.71777362108976000,-96.80840102118572000)");

        String value = ParserStrategyConfigDallas.LONGITUDE_CSV_311_DALLAS(row);
        assertThat(value).isEqualTo("-96.80840102118572000");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_DALLAS_TEST() {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_DALLAS).isNotNull();

        Map<String, String> row = Map.of("Date of Report", "2022-11-09 07:03:00.0000000");

        String value = ParserStrategyConfigDallas.REPORTED_AT_CSV_CRIME_DALLAS(row);
        assertThat(value).isEqualTo("2022-11-09T07:03:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTED_AT2_CSV_CRIME_DALLAS_TEST() {
        assertThat(ParserStrategy.REPORTED_AT2_CSV_CRIME_DALLAS).isNotNull();

        Map<String, String> row = Map.of("Date1 of Occurrence", "2016-09-16 00:00:00.0000000");

        String value = ParserStrategyConfigDallas.REPORTED_AT2_CSV_CRIME_DALLAS(row);
        assertThat(value).isEqualTo("2016-09-16T00:00:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTED_AT_CRIME_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CRIME_DALLAS).isNotNull();

        String jsonStr = "{\"reporteddate\":\"2016-07-19 17:22:00.0000000\",\"date1\":\"2016-07-19 00:00:00.0000000\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = ParserStrategyConfigDallas.REPORTED_AT_CRIME_DALLAS(item);
        assertThat(value).isEqualTo("2016-07-19T17:22:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @Transactional
    public void REPORTED_AT2_CRIME_DALLAS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT2_CRIME_DALLAS).isNotNull();
        String jsonStr = "{\"reporteddate\":\"2016-07-19 17:22:00.0000000\",\"date1\":\"2016-07-19 00:00:00.0000000\"}";
        JsonNode item = objectMapper.readTree(jsonStr);

        String value = ParserStrategyConfigDallas.REPORTED_AT2_CRIME_DALLAS(item);
        assertThat(value).isEqualTo("2016-07-19T00:00:00");

        // ensure it's parseable downstream
        LocalDateTime parsed = LocalDateTime.parse(value);
        assertThat(parsed).isInstanceOf(LocalDateTime.class);
    }

}
