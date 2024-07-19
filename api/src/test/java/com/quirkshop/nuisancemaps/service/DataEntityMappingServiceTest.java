package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import com.quirkshop.nuisancemaps.config.ParserStrategy;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.Mapping;

import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;


@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataEntityMappingServiceTest {

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
    private DataEntityMappingService dataEntityMappingService;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TextCategoryService textCategoryService;

    private List<Source> sources = new ArrayList<Source>();

    @BeforeAll
    public void setUpOnce() throws IOException {
        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON1 = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        File sourceJSON2 = resourceLoader.getResource("classpath:data/method_config.json").getFile();

        sources.addAll(objectMapper.readValue(sourceJSON1, new TypeReference<List<Source>>() {
        }));
        sources.addAll(objectMapper.readValue(sourceJSON2, new TypeReference<List<Source>>() {
        }));
        for (Source s : sources) {
            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }
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

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        categoryRepository.deleteAll();
        textCategoryRepository.deleteAll();
    }

    @Test
    @Transactional
    public void DataEntityMappingMethod() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/311-dallas.json");
        Source s = sourceRepository.findOneBySourceConfigId(4);

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "service_request_number");
        dataJobRepository.save(d);

        textCategoryService.refreshTextCategoryIdMap();

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);
        JsonNode rootNode = dataService.parseData(s, d, jsonResponse);

        JsonNode node = rootNode.get(0);
        GeometryFactory geometryFactory = new GeometryFactory();
        System.out.println("--NODE----");
        System.out.println(node);
        try {
            //IDataEntity dataEntity = dataEntityMappingService.buildDataEntity(Data311.class, s, node, geometryFactory);
            String reportNum = dataEntityMappingService.parseEntity(Mapping::getReportNum, s, node);
            System.out.println("REPORTNUM: " + reportNum);

            String result = dataEntityMappingService.parseNode(node, ParserStrategy.LATITUDE_311_DALLAS);
            System.out.println(result);


        } catch (Exception e) {

        }

    }

    @Test
    @Transactional
    public void buildDataEntityMissingCategoryException() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/311-atx.json");
        Source s = sourceRepository.findOneBySourceConfigId(2);

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "sr_number");
        dataJobRepository.save(d);

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // remove mapping - trigger MissingCategory exception
        textCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        textCategoryService.refreshTextCategoryIdMap();

        // trigger error with missing textCategory lookup in buildDataEntity
        JsonNode rootNode = dataService.parseData(s, d, jsonResponse);
        JsonNode node = rootNode.get(0);
        GeometryFactory geometryFactory = new GeometryFactory();
        assertThrows(MissingCategoryException.class, () -> {
            dataEntityMappingService.buildDataEntity(Data311.class, s, node, geometryFactory);
        });
    }

}
