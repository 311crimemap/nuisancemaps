package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileCopyUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
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

    @Autowired
    private DataJobRepository dataJobRepository;

    @Autowired
    private DataErrorRepository dataErrorRepository;

    @Test
    @Transactional
    public void createDataCrime() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");

        // Source
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(1);
        sourceRepository.save(s);

        // DataJob to crawl: replace job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(s, 1000, 100, "incident_report_number");
        dataJobRepository.save(d);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        int num = dataService.createData(s, d, jsonResponse);
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

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(s, 1000, 100, "sr_number");
        dataJobRepository.save(d);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        int num = dataService.createData(s, d, jsonResponse);
        assertThat(num).isEqualTo(2);
    }

    @Test
    @Transactional
    public void createData311DataError() throws IOException {

        // Source
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(2);
        sourceRepository.save(s);

        // trigger error with missing fields
        String jsonResponse = "[{ \"sr_missing_all_fields\": true}, { \"sr_missing_all_fields\": true}]";
        String objectMapperResponse = "{sr_missing_all_fields=true}";

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(s, 1000, 100, "sr_number");
        dataJobRepository.save(d);

        // dataService to create instances
        int num = dataService.createData(s, d, jsonResponse);
        assertThat(num).isEqualTo(0);

        // creates a dataError
        // job also exceeds error rate (100% here)
        List<DataError> dataErrors = dataErrorRepository.findAll();
        assertThat(dataErrors.size()).isEqualTo(2);
        assertThat(dataErrors.get(0).getContent()).isEqualTo(objectMapperResponse);
        assertThat(dataErrors.get(1).getContent()).isEqualTo(objectMapperResponse);
        assertThat(dataErrors.get(1).getErrorMsg()).contains("DataService.createData311");
        assertThat(d.getStatus()).isEqualTo(DataJobStatus.ERROR);
    }
}