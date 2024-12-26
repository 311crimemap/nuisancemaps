package com.quirkshop.nuisancemaps.service.parserstrategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserStrategyConfigMultiTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void REPORTED_AT_CSV_yyyyMMddHHmmssSSS_DASH_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_yyyyMMddHHmmssSSS_DASH).isNotNull();

        String field = "AnyDynamicField";
        String field2 = "field2";
        Map<String, String> row = Map.of(field, "2024-04-18 16:52:05.1974",
                field2, "2024-01-01 05:04:51.0");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_yyyyMMddHHmmssSSS_DASH(row, mappingField);
        assertThat(value).isEqualTo("2024-04-18T16:52:05");

        MappingField mappingField2 = new MappingField();
        mappingField2.setField(field2);

        String value2 = ParserStrategyConfigMulti.REPORTED_AT_CSV_yyyyMMddHHmmssSSS_DASH(row, mappingField2);
        assertThat(value2).isEqualTo("2024-01-01T05:04:51");
    }

    // 05/11/2024 19:47:00
    @Test
    @Transactional
    public void REPORTED_AT_CSV_MMddyyyyHHmmss_SLASH() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_MMddyyyyHHmmss_SLASH).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "05/11/2024 19:47:00");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_MMddyyyyHHmmss_SLASH(row, mappingField);
        assertThat(value).isEqualTo("2024-05-11T19:47:00");
    }

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

    // 1/3/2024
    @Test
    @Transactional
    public void REPORTED_AT_CSV_Mdyyyy_SLASH_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_Mdyyyy_SLASH).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "1/3/2024");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_Mdyyyy_SLASH(row, mappingField);
        assertThat(value).isEqualTo("2024-01-03T00:00:00");
    }

    // 2024-12-04
    @Test
    @Transactional
    public void REPORTED_AT_CSV_yyyyMMdd_DASH_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_yyyyMMdd_DASH).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "2024-12-04");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_yyyyMMdd_DASH(row, mappingField);
        assertThat(value).isEqualTo("2024-12-04T00:00:00");
    }

    // 2024-12-04
    @Test
    @Transactional
    public void REPORTED_AT_JSON_yyyyMMdd_DASH_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_JSON_yyyyMMdd_DASH).isNotNull();

        String field = "date_request_opened";
        ObjectNode node = objectMapper.createObjectNode();
        node.put(field, "2024-12-04");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);
        mappingField.setPointer("/" + field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_JSON_yyyyMMdd_DASH(node, mappingField);
        assertThat(value).isEqualTo("2024-12-04T00:00:00");
    }

    // Mar 31, 2017 08:21 AM
    @Test
    @Transactional
    public void REPORTED_AT_CSV_MMMddyyyyhhmma_SPACE_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.REPORTED_AT_CSV_MMMddyyyyhhmma_SPACE).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "Mar 31, 2017 08:21 AM");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.REPORTED_AT_CSV_MMMddyyyyhhmma_SPACE(row, mappingField);
        assertThat(value).isEqualTo("2017-03-31T08:21:00");
    }

    /*
     * POINT
     */

    // (37.7348199929233°, -122.2006649756992°)
    @Test
    @Transactional
    public void LATITUDE_CSV_COORDS_DEGREE_MULTI_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LATITUDE_CSV_COORDS_DEGREE_MULTI).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "(37.7348199929233°, -122.2006649756992°)");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.LATITUDE_CSV_COORDS_DEGREE_MULTI(row, mappingField);
        assertThat(value).isEqualTo("37.7348199929233");
    }

    // (37.7348199929233°, -122.2006649756992°)
    @Test
    @Transactional
    public void LONGITUDE_CSV_COORDS_DEGREE_MULTI_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_CSV_COORDS_DEGREE_MULTI).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "(37.7348199929233°, -122.2006649756992°)");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.LONGITUDE_CSV_COORDS_DEGREE_MULTI(row, mappingField);
        assertThat(value).isEqualTo("-122.2006649756992");
    }

    // (37.7348199929233, -122.2006649756992)
    @Test
    @Transactional
    public void LONGITUDE_CSV_COORDS_NODEGREE_MULTI_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_CSV_COORDS_DEGREE_MULTI).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "(37.7348199929233, -122.2006649756992)");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.LONGITUDE_CSV_COORDS_DEGREE_MULTI(row, mappingField);
        assertThat(value).isEqualTo("-122.2006649756992");
    }

    // convert SRX (lng) / SRY (lat)
    @Test
    @Transactional
    public void LATITUDE_CSV_EPSG_3857_TO_4326_MULTI_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LATITUDE_CSV_EPSG_3857_TO_4326_MULTI).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "4672104.222");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.LATITUDE_CSV_EPSG_3857_TO_4326_MULTI(row, mappingField);
        assertThat(value).isEqualTo("38.653113913413904");
    }

    @Test
    @Transactional
    public void LONGITUDE_CSV_EPSG_3857_TO_4326_MULTI_TEST() throws JsonMappingException, JsonProcessingException {
        assertThat(ParserStrategy.LONGITUDE_CSV_EPSG_3857_TO_4326_MULTI).isNotNull();

        String field = "AnyDynamicField";
        Map<String, String> row = Map.of(field, "-10044683.634");

        MappingField mappingField = new MappingField();
        mappingField.setField(field);

        String value = ParserStrategyConfigMulti.LONGITUDE_CSV_EPSG_3857_TO_4326_MULTI(row, mappingField);
        assertThat(value).isEqualTo("-90.23292832567417");
    }

}
