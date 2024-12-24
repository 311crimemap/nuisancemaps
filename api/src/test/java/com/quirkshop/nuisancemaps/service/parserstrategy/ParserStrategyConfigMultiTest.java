package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigMultiTest {

    @Test
    @Transactional
    public void REPORTED_AT_CSV_MMddyyyyhhmmssa_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_MMddyyyyhhmmssa).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "01/09/2022 01:18:38 PM");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_MMddyyyyhhmmssa(row, mappingField);
        assertThat(value).isEqualTo("2022-01-09T13:18:38");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_yyyyMMddHHmmssx_SLASH_TZ_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_yyyyMMddHHmmssx_SLASH_TZ).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "2022/07/12 19:04:00+00");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_yyyyMMddHHmmssx_SLASH_TZ(row, mappingField);
        assertThat(value).isEqualTo("2022-07-12T19:04:00");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_Mdyyyyhhmmssa_SLASH_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_Mdyyyyhhmmssa_SLASH).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "5/4/2022 5:54:30 PM");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_Mdyyyyhhmmssa_SLASH(row, mappingField);
        assertThat(value).isEqualTo("2022-05-04T17:54:30");
    }

    @Test
    @Transactional
    public void REPORTED_AT_CSV_MMddyyyy_SLASH_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_MMddyyyy_SLASH).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "05/04/2022");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_MMddyyyy_SLASH(row, mappingField);
        assertThat(value).isEqualTo("2022-05-04T00:00:00");
    }

}
