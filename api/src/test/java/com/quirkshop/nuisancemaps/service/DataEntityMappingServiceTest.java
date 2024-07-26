package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.Mapping;

import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
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
    private LocaleRepository localeRepository;

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

        sources.addAll(objectMapper.readValue(sourceJSON1, new TypeReference<List<Source>>() {
        }));

        for (Source s : sources) {
            Locale locale = new Locale();
            localeRepository.save(locale);
            s.setLocale(locale);
            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }

        // require textCategory mapping to exist before successful save
        // otherwise will throw MissingCategoryException and skip

        // atx
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

        // dallas (parser strategy)
        cat2 = new Category("311", "Code Violation", 1, null);
        categoryRepository.save(cat2);
        tc2 = new TextCategory("311", "Code Concern - CCS", cat2);
        TextCategory tc3 = new TextCategory("311", "Complaint/Compliment - 311", cat2);
        TextCategory tc4 = new TextCategory("311", "Water/Wastewater Line Locate - 311", cat2);
        TextCategory tc5 = new TextCategory("311", "Sanitation Roll Cart Maintenance/Delivery - SAN", cat2);
        textCategoryRepository.save(tc2);
        textCategoryRepository.save(tc3);
        textCategoryRepository.save(tc4);
        textCategoryRepository.save(tc5);
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
        textCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @Transactional
    public void BuildDataEntityParseEntityTest()
            throws IOException, NoSuchMethodException, IllegalAccessException, InstantiationException,
            InvocationTargetException, MissingReportCategoryException, MissingCategoryException,
            MissingCoordinateException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/311-dallas.json");
        Source s = sourceRepository.findOneBySourceConfigId(4);
        GeometryFactory geometryFactory = new GeometryFactory();

        // DataJob to crawl: stub job and fetch with json fixture response
        // Read the content of the JSON file vs actual fetch
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "service_request_number");
        dataJobRepository.save(d);
        textCategoryService.refreshTextCategoryIdMap();

        String jsonResponse = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);
        JsonNode rootNode = dataService.parseData(s, d, jsonResponse);

        for (JsonNode node : rootNode) {

            // test buildDataEntity matches parseEntity output
            Data311 data311 = (Data311) dataEntityMappingService
                    .buildDataEntity(Data311.class, s, node, geometryFactory);

            String reportNum = dataEntityMappingService.parseEntity(Mapping::getReportNum, s, node);
            String reportCategory = dataEntityMappingService.parseEntity(Mapping::getReportCategory, s, node);
            String description = dataEntityMappingService.parseEntity(Mapping::getDescription, s, node);
            String location = dataEntityMappingService.parseEntity(Mapping::getLocation, s, node);
            String lat = dataEntityMappingService.parseEntity(Mapping::getLatitude, s, node);
            String lng = dataEntityMappingService.parseEntity(Mapping::getLongitude, s, node);
            String reported_at1 = dataEntityMappingService.parseEntity(Mapping::getReportedAt, s, node);

            // accommodate for data type changes
            Double latitude = lat == null ? null : Double.parseDouble(lat);
            Double longitude = lng == null ? null : Double.parseDouble(lng);
            LocalDateTime reportedAt = reported_at1.isEmpty() ? null : LocalDateTime.parse(reported_at1);

            assertThat(reportNum).isEqualTo(data311.getReportNum());
            assertThat(reportCategory).isEqualTo(data311.getReportCategory());
            assertThat(description).isEqualTo(data311.getDescription());
            assertThat(location).isEqualTo(data311.getLocation());
            assertThat(latitude).isEqualTo(data311.getLatitude());
            assertThat(longitude).isEqualTo(data311.getLongitude());
            assertThat(reportedAt).isEqualTo(data311.getReportedAt());
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
