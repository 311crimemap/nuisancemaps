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
public class ParserStrategyConfigPrinceGeorgesTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void ADDRESS_JSON_CRIME_PRINCE_GEORGES_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.ADDRESS_JSON_CRIME_PRINCE_GEORGES).isNotNull();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("street_number", "1200 BLOCK");
        node.put("street_address", "MERCANTILE LN");

        String value = ParserStrategyConfigPrinceGeorges.ADDRESS_JSON_CRIME_PRINCE_GEORGES(node, null);
        assertThat(value).isEqualTo("1200 BLOCK MERCANTILE LN");
    }

    @Test
    @Transactional
    public void ADDRESS_JSON_CRIME_NO_ADDRESS_PRINCE_GEORGES_TEST()
            throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.ADDRESS_JSON_CRIME_PRINCE_GEORGES).isNotNull();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("street_address", "MERCANTILE LN");

        String value = ParserStrategyConfigPrinceGeorges.ADDRESS_JSON_CRIME_PRINCE_GEORGES(node, null);
        assertThat(value).isEqualTo("MERCANTILE LN");
    }
}
