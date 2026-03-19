package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import com.quirkshop.nuisancemaps.model.datajob.BaseConfigurator;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.ERSI_FeatureServerConfigurator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = NuisancemapsApplication.class)
public class ERSI_DataJobConfiguratorTest {

    @Autowired
    private ResourceLoader resourceLoader;

    Source source;

    @BeforeAll
    public void setUp() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/311-memphis-source-config.json").getFile();
        source = objectMapper.readValue(sourceJSON, new TypeReference<Source>() {
        });

    }

    @Test
    public void ERSI_FeatureServerConfigurator_initialize_test() {

        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "OBJECTID");
        ERSI_FeatureServerConfigurator ersiConfigurator = new ERSI_FeatureServerConfigurator();

        dataJob = ersiConfigurator.initialize(dataJob);
        String outFields = ersiConfigurator.buildURLFields(source.getMapping());
        String canonURL = "https://311.memphistn.gov/server/rest/services/311/311_Request_Map_PROD/FeatureServer/0/query?f=json&where=1=1&returnGeometry=false&outFields="
                + outFields
                + "&orderByFields=OBJECTID ASC&resultOffset=0&resultRecordCount=32000&resultType=standard";

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

    @Test
    public void ERSI_FeatureServer_next_test() {
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "OBJECTID");

        ERSI_FeatureServerConfigurator ersiConfigurator = new ERSI_FeatureServerConfigurator();
        dataJob = ersiConfigurator.initialize(dataJob); // offset 0
        dataJob = ersiConfigurator.next(dataJob); // next batch, offset limit
        dataJob = ersiConfigurator.next(dataJob); // batch 3: offset 2*limit
        String outFields = ersiConfigurator.buildURLFields(source.getMapping());
        String canonURL = "https://311.memphistn.gov/server/rest/services/311/311_Request_Map_PROD/FeatureServer/0/query?f=json&where=1=1&returnGeometry=false&outFields="
                + outFields
                + "&orderByFields=OBJECTID ASC&resultOffset=64000&resultRecordCount=32000&resultType=standard";

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

}
