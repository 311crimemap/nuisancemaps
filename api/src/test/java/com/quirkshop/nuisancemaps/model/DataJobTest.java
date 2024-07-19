package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void DataJobBuildURLTest() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        List<Source> sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {});

        Source s = sources.get(0);
        final int limit = 10000;
        final int offset = 20000;
        final String order_key = "id";

        DataJob d = new DataJob(LocalDateTime.now(), s, limit, offset, order_key);
        String url = d.buildURL();
        final String select = d.buildURLFields(s.getMapping());

        assertThat(s.getUrl()).isEqualTo(d.getSourceURL());
        assertThat(url).isEqualTo(
                s.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" + order_key + "&$select=" + select);
        assertThat(d.getUrl()).isEqualTo(url);
        assertThat(select).isNotBlank();

    }
}
