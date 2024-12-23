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
public class ParserStrategyConfigMemphisTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    // 02/05/2018 08:58:00 AM
    @Test
    @Transactional
    public void REPORTED_AT_CSV_CRIME_MEMPHIS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_CRIME_MEMPHIS).isNotNull();

        Map<String, String> row = Map.of("Offense Date", "02/05/2018 08:58:00 AM");

        String value = ParserStrategyConfigMemphis.REPORTED_AT_CSV_CRIME_MEMPHIS(row, null);
        assertThat(value).isEqualTo("2018-02-05T08:58:00");
    }

    // 01/05/2016 09:11:11 PM
    @Test
    @Transactional
    public void REPORTED_AT_CSV_311_MEMPHIS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_311_MEMPHIS).isNotNull();

        Map<String, String> row = Map.of("REPORTED_DATE", "02/05/2018 08:58:00 AM");

        String value = ParserStrategyConfigMemphis.REPORTED_AT_CSV_311_MEMPHIS(row, null);
        assertThat(value).isEqualTo("2018-02-05T08:58:00");
    }

    // POINT (-90.04925 35.14976)
    @Test
    @Transactional
    public void LONGITUDE_CSV_311_MEMPHIS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_CSV_311_MEMPHIS).isNotNull();

        Map<String, String> row = Map.of("location1", "POINT (-90.04925 35.14976)");

        String value = ParserStrategyConfigMemphis.LONGITUDE_CSV_311_MEMPHIS(row, null);
        assertThat(value).isEqualTo("-90.04925");
    }

    // POINT (-90.04925 35.14976)
    @Test
    @Transactional
    public void LATITUDE_CSV_311_MEMPHIS_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LATITIUDE_CSV_311_MEMPHIS).isNotNull();

        Map<String, String> row = Map.of("location1", "POINT (-90.04925 35.14976)");

        String value = ParserStrategyConfigMemphis.LATITIUDE_CSV_311_MEMPHIS(row, null);
        assertThat(value).isEqualTo("35.14976");
    }

}
