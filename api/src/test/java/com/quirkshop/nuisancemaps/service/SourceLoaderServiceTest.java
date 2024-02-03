package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class SourceLoaderServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
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
    public void fetchCountTest() throws UnsupportedEncodingException {
        int val = 123;
        String jsonFixtureContent = String.format("[ { \"count_incident_report_number\" : \"%s\"} ]", val);

        sourceLoaderService.loadJSON("data/source_config.json");
        HashMap<Integer, Source> sourceMap = sourceLoaderService.getSourceMap();
        Source s = sourceMap.get(1);
        String report_num = s.getMapping().get("report_num").toString();
        String url = sourceLoaderService.buildCountURL(s.getUrl(), report_num);

        when(restTemplate.getForObject(url, String.class))
                .thenReturn(jsonFixtureContent);

        int num = sourceLoaderService.fetchCount(s);

        assertThat(num).isEqualTo(val);
    }

    @Test
    public void findBySourceConfigIDTest() {
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(1);
        assertThat(s.getSourceConfigId()).isEqualTo(1);
    }

}
