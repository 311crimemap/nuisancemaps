package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigMontgomeryCountyTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    //
    @Test
    @Transactional
    public void ADDRESS_CSV_CRIME_MONTGOMERY_COUNTY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.ADDRESS_CSV_CRIME_MONTGOMERY_COUNTY).isNotNull();

        Map<String, String> row = Map.of("Block Address", "123 ABC ST",
                "City", "myCity",
                "State", "MD",
                "Zip Code", "12345");

        String value = ParserStrategyConfigMontgomeryCounty.ADDRESS_CSV_CRIME_MONTGOMERY_COUNTY(row, null);
        assertThat(value).isEqualTo("123 ABC ST myCity MD 12345");
    }

    //
    @Test
    @Transactional
    public void ADDRESS_JSON_CRIME_MONTGOMERY_COUNTY_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.ADDRESS_JSON_CRIME_MONTGOMERY_COUNTY).isNotNull();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("location", "123 ABC ST");
        node.put("city", "myCity");
        node.put("state", "MD");
        node.put("zip_code", "12345");

        String value = ParserStrategyConfigMontgomeryCounty.ADDRESS_JSON_CRIME_MONTGOMERY_COUNTY(node, null);
        assertThat(value).isEqualTo("123 ABC ST myCity MD 12345");
    }

}
