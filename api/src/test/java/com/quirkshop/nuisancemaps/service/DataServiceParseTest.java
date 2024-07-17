package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataJob;

import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataServiceParseTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DataService dataService;

    @Test
    @Transactional
    public void parseDataNested() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-dallas.json");
        Source source = new Source();
        DataJob dataJob = new DataJob();

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        List<Map<String, Object>> responseList = dataService.parseData(source, dataJob, jsonResponse);

        // NB: asText returns "" empty string, or what is set as defaultValue (null)
        for (Map<String, Object> responseObject : responseList) {

            // get nested object
            // Map<String, Object> responseObject = responseList.get(0);
            Map<String, Object> geoCodedColumnObject = (Map<String, Object>) responseObject.get("geocoded_column");

            if (geoCodedColumnObject == null) {

                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(responseObject);
                JsonNode rootNode = mapper.readTree(jsonString);
                JsonNode latitudeNode = rootNode.at("/geocoded_column/latitude");
                assertThat(latitudeNode.asText(null)).isNull();

                continue;
            }

            // Test nested parsing
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(responseObject);
            JsonNode rootNode = mapper.readTree(jsonString);
            JsonNode latitudeNode = rootNode.at("/geocoded_column/latitude");

            assertThat(geoCodedColumnObject.get("latitude")).isEqualTo(latitudeNode.asText(null));
        }

    }

    @Test
    @Transactional
    public void parseDataList() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-dallas.json");

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);

        for (JsonNode item : rootNode) {
            JsonNode latitudeNode = item.at("/geocoded_column/latitude");
            if (latitudeNode.isMissingNode()) {
                continue;
            }

            assertThat(latitudeNode.isNumber());
        }

    }
}
