package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobTest {

    @Autowired
    private ResourceLoader resourceLoader;

    List<Source> sources;

    @BeforeAll
    public void setUp() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });

    }

    @Test
    public void DataJobBuildURLTest() throws IOException {

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

    @Test
    public void DataJobBuildFilenameTest() throws IOException {

        Source source = sources.get(11); // id: 12
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, 0, 0, "id");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-mm");
        String formattedDateTime = now.format(formatter);

        // String result = "data.cityofnewyork.us-api-views-5uac-w243-rows.csv";
        String result = String.format("%s-%s.csv",
                "data.cityofnewyork.us-api-views-5uac-w243-rows",
                formattedDateTime);

        String filename = dataJob.buildFilename();
        assertThat(filename).isEqualTo(result);

        String url2 = "https://data.sfgov.org/resource/vw6y-z8j6.json";
        source.setDataParserType(DataParserType.JSON);
        source.setUrl(url2);
        String result2 = String.format("%s-%s.json",
                "data.sfgov.org-resource-vw6y-z8j6",
                formattedDateTime);

        String filename2 = dataJob.buildFilename();
        assertThat(filename2).isEqualTo(result2);
    }

}
