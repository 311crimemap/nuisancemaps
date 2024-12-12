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
public class XLSDataParserTest {

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
    TextCategoryService textCategoryService;

    @Autowired
    private XLSDataParser xlsDataParser;

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

        // require textCategory mapping to exist before successful save
        // otherwise will throw MissingCategoryException and skip
        Category cat = new Category("crime", "Public Order", 0, null);
        categoryRepository.save(cat);

        ArrayList<TextCategory> textCategories = new ArrayList<TextCategory>();
        textCategories.add(new TextCategory("crime", "Destruction, damage, vandalism", cat));
        textCategories.add(new TextCategory("crime", "Intimidation", cat));
        textCategories.add(new TextCategory("crime", "Burglary, Breaking and Entering", cat));
        textCategories.add(new TextCategory("crime", "Disorderly conduct", cat)); // 2
        textCategories.add(new TextCategory("crime", "All other offenses", cat));

        textCategoryRepository.saveAll(textCategories);

        textCategoryService.initMaps();
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
        textCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        pendingTextCategoryRepository.deleteAll();
    }

    @Test
    @Transactional
    public void parseTest() throws IOException {

        // xls has 6 records total
        Resource csvResource = resourceLoader.getResource("classpath:data/NIBRSPublicView2024.xlsx");
        InputStream inputstream = csvResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(21);
        DataJob d = new DataJob(LocalDateTime.now(), s, "Incident");
        dataJobRepository.save(d);

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        xlsDataParser.parse(d, inputstream, parseCounter);

        assertThat(dataCrimeRepository.count()).isEqualTo(6);
    }

}
