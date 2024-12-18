package com.quirkshop.nuisancemaps.service.dataparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.PendingTextCategory;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.PendingTextCategoryRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.service.TextCategoryService;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSONDataParserTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private DataJobRepository dataJobRepository;

    @Autowired
    private DataCrimeRepository dataCrimeRepository;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private PendingTextCategoryRepository pendingTextCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    protected TextCategoryService textCategoryService;

    @Autowired
    private JSONDataParser jsonDataParser;

    private List<Source> sources;

    @BeforeAll
    public void setUpOnce() throws IOException {
        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config_archive.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });
        for (Source s : sources) {
            Locale locale = new Locale();
            localeRepository.save(locale);
            s.setLocale(locale);
            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }

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

        tc = new TextCategory("crime", "BURGLARY NON RESIDENCE", cat2);
        tc2 = new TextCategory("crime", "BURGLARY OF RESIDENCE", cat2);
        TextCategory tc3 = new TextCategory("crime", "BURGLARY OF SHED/DETACHED GARAGE/STORAGE UNIT", cat2);
        textCategoryRepository.save(tc);
        textCategoryRepository.save(tc2);
        textCategoryRepository.save(tc3);
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
        textCategoryRepository.deleteAll();
        pendingTextCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @Transactional
    public void parseTest() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");
        InputStream inputstream = jsonResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(1);
        DataJob d = new DataJob(LocalDateTime.now(), s, "incident_report_number");
        dataJobRepository.save(d);

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        jsonDataParser.parse(d, null, inputstream, parseCounter);

        assertThat(dataCrimeRepository.count()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void parseERSITest() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/ersi-2024-07-01-2024-08-15-atx.json");
        InputStream inputstream = jsonResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(15);
        DataJob d = new DataJob(LocalDateTime.now(), s, "objectid");
        dataJobRepository.save(d);

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        jsonDataParser.parse(d, null, inputstream, parseCounter);

        assertThat(dataCrimeRepository.count()).isEqualTo(5);
    }

    @Test
    @Transactional
    public void parsePendingTextCategoryTest() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");
        InputStream inputstream = jsonResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(1);
        DataJob d = new DataJob(LocalDateTime.now(), s, "incident_report_number");
        dataJobRepository.save(d);

        TextCategory tc = textCategoryRepository.findByDataTypeAndText("crime", "DWI 2ND");
        textCategoryRepository.delete(tc);
        textCategoryService.initMaps();

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        jsonDataParser.parse(d, null, inputstream, parseCounter);

        // missing a TextCategory, no longer saves the parsed dataEntity
        assertThat(dataCrimeRepository.count()).isEqualTo(1);

        // check PendingTextCategory - doesn't save 1 because of missing TC
        Iterable<PendingTextCategory> ptcIter = pendingTextCategoryRepository.findAll();
        List<PendingTextCategory> ptcs = new ArrayList<PendingTextCategory>();
        ptcIter.forEach(ptcs::add);

        assertThat(ptcs.size()).isEqualTo(1);
        assertThat(ptcs.get(0).getText()).isEqualTo("DWI 2ND");
    }

}
