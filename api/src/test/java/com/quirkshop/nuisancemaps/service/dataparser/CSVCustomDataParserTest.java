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
import com.quirkshop.nuisancemaps.repository.Data311Repository;
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
public class CSVCustomDataParserTest {

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
    private Data311Repository data311Repository;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private PendingTextCategoryRepository pendingTextCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    TextCategoryService textCategoryService;

    @Autowired
    private CSVCustomDataParser csvCustomDataParser;

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
        Category cat = new Category("311", "Infrastructure and Maintenance", 0, null);
        categoryRepository.save(cat);

        ArrayList<TextCategory> textCategories = new ArrayList<TextCategory>();
        textCategories.add(new TextCategory("311", "Sewer Wastewater", cat));
        textCategories.add(new TextCategory("311", "Trash Dumping or Illegal Dumpsite", cat));
        textCategories.add(new TextCategory("311", "Water Service", cat));
        textCategories.add(new TextCategory("311", "GRAND LARCENY", cat));
        textCategories.add(new TextCategory("311", "Missed Heavy Trash Pickup", cat));
        textCategories.add(new TextCategory("311", "Liaison Comm", cat));
        textCategories.add(new TextCategory("311", "Sign Code Violation", cat));
        textCategories.add(new TextCategory("311", "Missed Garbage Pickup", cat));

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

        // csv has 10 records total, missing 1 text category
        Resource csvResource = resourceLoader.getResource("classpath:data/houston-311.txt");
        InputStream inputstream = csvResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(22);
        DataJob d = new DataJob(LocalDateTime.now(), s, "365 Case Number");
        dataJobRepository.save(d);

        assertThat(data311Repository.count()).isEqualTo(0);

        csvCustomDataParser.parse(d, null, inputstream, parseCounter);

        assertThat(data311Repository.count()).isEqualTo(6);
    }

    @Test
    @Transactional
    public void parseCSVCustomMissingClosingQuoteTest() throws IOException {

        // csv has 7 records total
        // testing unclosed breaking quote parse string:
        // "The Solid Waste Department is aware of the delay and is diligen
        Resource csvResource = resourceLoader.getResource("classpath:data/csvcustom-quote.csv");
        InputStream inputstream = csvResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(22);

        DataJob d = new DataJob(LocalDateTime.now(), s, "365 Case Number");
        dataJobRepository.save(d);

        assertThat(data311Repository.count()).isEqualTo(0);

        csvCustomDataParser.parse(d, null, inputstream, parseCounter);

        assertThat(data311Repository.count()).isEqualTo(7);
    }

}
