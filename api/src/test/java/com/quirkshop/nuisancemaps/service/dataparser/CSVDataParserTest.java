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
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
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
public class CSVDataParserTest {

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
    private CSVDataParser csvDataParser;

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
        textCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() throws IOException {
        // require textCategory mapping to exist before successful save
        // otherwise will throw MissingCategoryException and skip
        Category cat = new Category("crime", "Public Order", 0, null);
        categoryRepository.save(cat);

        ArrayList<TextCategory> textCategories = new ArrayList<TextCategory>();
        textCategories.add(new TextCategory("crime", "SEX CRIMES", cat));
        textCategories.add(new TextCategory("crime", "HARRASSMENT 2", cat));
        textCategories.add(new TextCategory("crime", "PETIT LARCENY", cat));
        textCategories.add(new TextCategory("crime", "GRAND LARCENY", cat));
        textCategories.add(new TextCategory("crime", "MURDER & NON-NEGL. MANSLAUGHTER", cat));
        textCategories.add(new TextCategory("crime", "CRIMINAL MISCHIEF & RELATED OF", cat));
        textCategories.add(new TextCategory("crime", "GRAND LARCENY OF MOTOR VEHICLE", cat));
        textCategories.add(new TextCategory("crime", "OFF. AGNST PUB ORD SENSBLTY &", cat));

        textCategoryRepository.saveAll(textCategories);
    }

    @Test
    @Transactional
    public void parse() throws IOException {

        Resource csvResource = resourceLoader.getResource("classpath:data/crime-nyc.csv");
        InputStream inputstream = csvResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(12);
        DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 0, "CMPLNT_NUM");
        dataJobRepository.save(d);

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        csvDataParser.parse(d, inputstream, parseCounter);

        assertThat(dataCrimeRepository.count()).isEqualTo(9);
    }
}
