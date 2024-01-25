package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileCopyUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataServiceTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private SourceLoaderService sourceLoaderService;

    @Autowired
    private DataService dataService;

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    @Transactional
    public void createDataCrime() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");

        // Source
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(1);
        sourceRepository.save(s);

        // DataJob to crawl: replace job and fetch with json fixture response
        // DataJob datajob = new DataJob(s, 0, 0, "id");
        // Read the content of the JSON file vs actual fetch
        // String jsonResponse = dataJobRequestService.fetchJSON(datajob);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        int num = dataService.createData(s, jsonResponse);
        assertThat(num).isEqualTo(2);
    }

    @Test
    @Transactional
    public void createData311() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/311-atx.json");

        // Source
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(2);
        sourceRepository.save(s);

        // DataJob to crawl: replace job and fetch with json fixture response
        // DataJob datajob = new DataJob(s, 0, 0, "id");
        // Read the content of the JSON file vs actual fetch
        // String jsonResponse = dataJobRequestService.fetchJSON(datajob);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        int num = dataService.createData(s, jsonResponse);
        assertThat(num).isEqualTo(2);
    }
}
