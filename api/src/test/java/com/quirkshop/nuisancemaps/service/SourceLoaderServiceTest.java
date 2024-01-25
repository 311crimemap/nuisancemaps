package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class SourceLoaderServiceTest {

    @Autowired
    private SourceLoaderService sourceLoaderService;

    @Test
    public void loadJSONTest() {
        sourceLoaderService.loadJSON("data/source_config.json");
        assertThat(sourceLoaderService.getSourceMap()).isNotNull();
        HashMap<Integer, Source> sourceMap = sourceLoaderService.getSourceMap();
        Source s = sourceMap.get(1);
        assertThat(s).isInstanceOf(Source.class);
        Map<String, Object> m = s.getMapping();
        assertThat(m.get("report_num").toString()).isEqualTo("incident_report_number");
    }


    @Test
    public void findBySourceConfigIDTest() {
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(1);
        assertThat(s.getSource_config_id()).isEqualTo(1);
    }

}
