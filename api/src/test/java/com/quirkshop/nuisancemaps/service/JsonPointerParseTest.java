package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
import org.springframework.util.FileCopyUtils;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonPointerParseTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @Transactional
    public void parseDataNestedList() throws IOException {

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
