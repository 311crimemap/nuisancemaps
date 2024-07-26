package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import org.springframework.core.io.ResourceLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SourceLoaderServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @InjectMocks // NB: doesn't Autowire, but will inject any @Mocks annotated here
    private SourceLoaderService sourceLoaderService;

    private List<Source> sources;

    @BeforeAll
    public void setUpOnce() throws IOException {
        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });
        for (Source s : sources) {
            Locale locale = new Locale();
            localeRepository.save(locale);
            s.setLocale(locale);

            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
    }

    @Test
    @Transactional
    public void fetchCountTest() throws UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
        int val = 123;
        String jsonFixtureContent = String.format("[ { \"count_incident_report_number\" : \"%s\"} ]", val);

        Source s = sourceRepository.findOneBySourceConfigId(1);
        String report_num = s.getMapping().getReportNum().getField();
        String url = sourceLoaderService.buildCountURL(s.getUrl(), report_num);

        when(restTemplate.getForObject(url, String.class))
                .thenReturn(jsonFixtureContent);

        /*
         *  tracing SourceLoaderService.fetchCount()
         */

        // Create the root and item nodes (rootNode.get(0))
        JsonNode rootNode = mock(JsonNode.class);
        JsonNode mockItemNode = mock(JsonNode.class);

        // set mock return object
        when(objectMapper.readTree(jsonFixtureContent)).thenReturn(rootNode);
        when(rootNode.get(0)).thenReturn(mockItemNode);

        // set nodes to return the value
        when(mockItemNode.at("/count_incident_report_number")).thenReturn(mockItemNode);
        when(mockItemNode.asInt()).thenReturn(val);

        int num = sourceLoaderService.fetchCount(s);

        assertThat(num).isEqualTo(val);
    }
}
