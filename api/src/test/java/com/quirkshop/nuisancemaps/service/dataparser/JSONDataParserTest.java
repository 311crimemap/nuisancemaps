package com.quirkshop.nuisancemaps.service.dataparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    private CategoryRepository categoryRepository;

    @Autowired
    private JSONDataParser jsonDataParser;

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
    public void parse() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");
        InputStream inputstream = jsonResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(1);
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "incident_report_number");
        dataJobRepository.save(d);

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        jsonDataParser.parse(d, inputstream, parseCounter);

        assertThat(dataCrimeRepository.count()).isEqualTo(2);
    }
}
