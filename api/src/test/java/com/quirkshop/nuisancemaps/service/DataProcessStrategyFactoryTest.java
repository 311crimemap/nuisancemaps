package com.quirkshop.nuisancemaps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.dataprocess.DataProcessStrategy;
import com.quirkshop.nuisancemaps.service.dataprocess.DataProcessStrategyFactory;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataProcessStrategyFactoryTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private DataCrimeRepository datacrime_repo;

    @MockBean
    private SourceRepository source_repo;

    @MockBean
    DataJobRepository dataJobRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DataProcessStrategyFactory dataProcessStrategyFactory;

    @Test
    @Transactional
    void testFetchDataWithMock() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");

        // Read the content of the JSON file
        String jsonFixtureContent = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();

        List<Source> sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });

        Source s = sources.get(0);

        when(source_repo.save(Mockito.any(Source.class))).thenReturn(s);

        // DataJob
        DataJob datajob = new DataJob(LocalDateTime.now(), s, 100, 50, "id");
        datajob.buildURL();
        assertThat(datajob.getStatus()).isEqualTo(DataJobStatus.QUEUED);

        // Mock restTemplate to return the jsonFixtureContent if it ever makes a request
        // to url
        // this @MockBean restTemplate is D.I'd into dataProcessStrategy.fetch(s)
        // below
        when(restTemplate.getForObject(datajob.getUrl(), String.class))
                .thenReturn(jsonFixtureContent);

        // build mock save result
        // DataCrime d = new DataCrime();
        // when(datacrime_repo.save(Mockito.any(DataCrime.class))).thenReturn(d);

        DataProcessStrategy dataProcessStrategy = dataProcessStrategyFactory
                .getStrategy(DataProcessType.MEMORY);
        String result = dataProcessStrategy.fetch(datajob);

        assertThat(result).isEqualTo(jsonFixtureContent);
        assertThat(datajob.getStatus()).isEqualTo(DataJobStatus.FETCH_COMPLETE);
        // int num = dataJobRequestService.createData();

        // num elements in fixture crime-atx
        // assertThat(num).isEqualTo(2);

        // verify(restTemplate).getForObject(datajob.getUrl(), String.class);

    }
}
