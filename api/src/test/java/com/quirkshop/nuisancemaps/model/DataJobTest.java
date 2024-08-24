package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.OpenDataConfigurator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

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

        DataJob dataJob = new DataJob(LocalDateTime.now(), s, order_key);
        HashMap<String, Object> parameters = dataJob.getParameters();
        parameters.put("paramLimit", limit);
        parameters.put("paramOffset", offset);

        OpenDataConfigurator openDataConfigurator = new OpenDataConfigurator();
        dataJob = openDataConfigurator.initialize(dataJob);
        final String select = openDataConfigurator.buildURLFields(s.getMapping());

        assertThat(s.getUrl()).isEqualTo(dataJob.getSourceURL());
        assertThat(dataJob.getUrl()).isEqualTo(
                s.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" + order_key + "&$select=" + select);
        assertThat(select).isNotBlank();
    }

    @Test
    public void DataJobBuildFilenameTest() throws IOException {

        Source source = sources.get(11); // id: 12, params
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-mm");
        String formattedDateTime = dataJob.getSessionId().format(formatter);
        String params = new URL(source.getUrl()).getQuery()
                .replaceAll("&", "__").replaceAll("=", "_");

        String result = String.format("%s-%s-%s.csv",
                "data.cityofnewyork.us-api-views-5uac-w243-rows",
                params,
                formattedDateTime);

        String filename = dataJob.buildFilename();
        assertThat(filename).isEqualTo(result);

        String url2 = "https://data.sfgov.org/resource/vw6y-z8j6.json"; //no params
        source.setDataParserType(DataParserType.JSON);
        source.setUrl(url2);
        String result2 = String.format("%s-%s.json",
                "data.sfgov.org-resource-vw6y-z8j6",
                formattedDateTime);

        String filename2 = dataJob.buildFilename();
        assertThat(filename2).isEqualTo(result2);
    }

}
