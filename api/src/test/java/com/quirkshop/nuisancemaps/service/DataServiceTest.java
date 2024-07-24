package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataServiceTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DataService dataService;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private DataJobRepository dataJobRepository;

    @Autowired
    private DataCrimeRepository dataCrimeRepository;

    @Autowired
    private DataErrorRepository dataErrorRepository;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private List<Source> sources;

    @BeforeAll
    public void setUpOnce() throws IOException {
        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });
        for (Source s : sources) {
            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() throws IOException {
        // require textCategory mapping to exist before successful save
        // otherwise will throw MissingCategoryException and skip
        Category cat = new Category("crime", "Public Order", 0, null);
        Category cat2 = new Category("crime", "Theft", 1, null);
        categoryRepository.save(cat);
        categoryRepository.save(cat2);
        TextCategory tc = new TextCategory("crime", "DWI 2ND", cat);
        TextCategory tc2 = new TextCategory("crime", "THEFT BY SHOPLIFTING", cat2);
        textCategoryRepository.save(tc);
        textCategoryRepository.save(tc2);

        cat = new Category("311", "Noise", 1, null);
        categoryRepository.save(cat);
        tc = new TextCategory("311", "APD - Non Emergency Noise/Alarm", cat);
        textCategoryRepository.save(tc);
    }

    @Test
    @Transactional
    public void createDataCrime() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");

        Source s = sourceRepository.findOneBySourceConfigId(1);

        // DataJob to crawl: replace job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "incident_report_number");
        dataJobRepository.save(d);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        dataService.createData(s, d, jsonResponse);
        assertThat(d.getNumProcessed()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void createData311() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/311-atx.json");

        Source s = sourceRepository.findOneBySourceConfigId(2);

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "sr_number");
        dataJobRepository.save(d);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        dataService.createData(s, d, jsonResponse);
        assertThat(d.getNumProcessed()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void createData311Entity() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/311-atx.json");

        Source s = sourceRepository.findOneBySourceConfigId(2);

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "sr_number");
        dataJobRepository.save(d);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // dataService to create instances
        dataService.createData(s, d, jsonResponse);
        Iterable<DataCrime> dataCrimesIter = dataCrimeRepository.findAll();

        // ensure srid and proper ordering of long/lat
        dataCrimesIter.forEach(dataCrime -> {
            assertThat(dataCrime.getPoint().getSRID()).isEqualTo(4326);
            assertThat(dataCrime.getPoint().getX()).isEqualTo(dataCrime.getLongitude());
            assertThat(dataCrime.getPoint().getY()).isEqualTo(dataCrime.getLatitude());
        });
    }

    @Test
    @Transactional
    public void createDataEntitiesError() throws IOException {

        Source s = sourceRepository.findOneBySourceConfigId(1);

        // trigger error with bad fields - latitude as String
        // we have additional fields reportCateogry, lat/lng that trigger Exceptions,
        // but
        // we explicitly don't want to create DataError objects with those
        String jsonResponse = "[{ \"sr_missing_all_fields\": true, \"reportCategory\": \"test\", \"latitude\": \"abc\", \"longitude\": 456}, { \"sr_missing_all_fields\": true, \"reportCategory\": \"test\", \"latitude\": \"abc\", \"longitude\": 456}]";
        String objectMapperResponse = "{\"sr_missing_all_fields\":true,\"reportCategory\":\"test\",\"latitude\":\"abc\",\"longitude\":456}";

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "sr_number");
        dataJobRepository.save(d);

        // dataService to create instances
        dataService.createData(s, d, jsonResponse);
        assertThat(d.getNumFetched()).isEqualTo(2);
        assertThat(d.getNumProcessed()).isEqualTo(0);

        // creates a dataError
        // on MissingCategoryError (now excluding MissingReportCategoryError and
        // MissingCoordinateError because its too commonplace)
        // job also exceeds error rate (100% here)
        List<DataError> dataErrors = dataErrorRepository.findAll();
        assertThat(dataErrors.size()).isEqualTo(2);
        assertThat(dataErrors.get(0).getContent()).isEqualTo(objectMapperResponse);
        assertThat(dataErrors.get(1).getContent()).isEqualTo(objectMapperResponse);
        assertThat(dataErrors.get(1).getErrorMsg()).contains("DataService.createDataEntities");
        assertThat(d.getStatus()).isEqualTo(DataJobStatus.ERROR);
    }
}
