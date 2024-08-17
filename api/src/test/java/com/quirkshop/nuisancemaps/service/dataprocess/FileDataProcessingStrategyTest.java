package com.quirkshop.nuisancemaps.service.dataprocess;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.service.dataparser.DataParserFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileDataProcessingStrategyTest {

    @Mock
    private FileStoreProvider fileStoreProvider;

    @Mock
    private FileStore fs;

    @Mock
    private DataJob mockDataJob;

    @MockBean
    private DataJobRepository mockDataJobRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private DataJobRepository dataJobRepository;

    @Autowired
    private DataCrimeRepository dataCrimeRepository;

    @Autowired
    private Data311Repository data311Repository;

    @Autowired
    private DataParserFactory dataParserFactory;

    @InjectMocks
    FileDataProcessStrategy fileDataProcessStrategy;

    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");
    private static final long DATA_DIR_MIN_FREE = Long.parseLong(System.getenv("DATA_DIR_MIN_FREE"));

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

        Category cat = new Category("crime", "Public Order", 0, null);
        categoryRepository.save(cat);

        ArrayList<TextCategory> textCategories = new ArrayList<TextCategory>();
        textCategories.add(new TextCategory("crime", "RAPE", cat));
        textCategories.add(new TextCategory("crime", "SEX CRIMES", cat));
        textCategories.add(new TextCategory("crime", "HARRASSMENT 2", cat));
        textCategories.add(new TextCategory("crime", "PETIT LARCENY", cat));
        textCategories.add(new TextCategory("crime", "GRAND LARCENY", cat));
        textCategories.add(new TextCategory("crime", "MURDER & NON-NEGL. MANSLAUGHTER", cat));
        textCategories.add(new TextCategory("crime", "CRIMINAL MISCHIEF & RELATED OF", cat));
        textCategories.add(new TextCategory("crime", "GRAND LARCENY OF MOTOR VEHICLE", cat));
        textCategories.add(new TextCategory("crime", "OFF. AGNST PUB ORD SENSBLTY &", cat));

        // 311
        Category cat2 = new Category("311", "Street Repair", 0, null);
        categoryRepository.save(cat2);

        textCategories.add(new TextCategory("311", "Street Condition", cat2));
        textCategoryRepository.saveAll(textCategories);

    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
        textCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        dataJobRepository.deleteAll();
    }

    @Test
    @Transactional
    public void validDiskSpaceTest() throws IOException {

        Path path = Paths.get(FETCH_DATA_DIR);
        when(fileStoreProvider.getFileStore(path)).thenReturn(fs);

        // valid
        when(fs.getUsableSpace()).thenReturn(DATA_DIR_MIN_FREE + 1);
        boolean valid = fileDataProcessStrategy.validDiskSpace();
        assertThat(valid).isTrue();

        // invalid
        when(fs.getUsableSpace()).thenReturn(DATA_DIR_MIN_FREE - 1L);
        valid = fileDataProcessStrategy.validDiskSpace();
        assertThat(valid).isFalse();

    }

    @Test
    @Transactional
    public void writeToFileTest() throws IOException {
        // generic content
        InputStream inputStream = resourceLoader
                .getResource("classpath:data/source_config.json").getInputStream();

        Source source = sourceRepository.findOneBySourceConfigId(12);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, 0, 0, "id");
        String filename = "test-" + dataJob.buildFilename();
        String filePath = String.join("/", FETCH_DATA_DIR, filename);
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
        }

        fileDataProcessStrategy.writeToFile(filePath, inputStream);

        assertThat(file.exists()).isTrue();
        assertThat(file.isFile()).isTrue();

        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @Transactional
    public void processCrimeNYCTest() throws IOException {

        Resource csvResource = resourceLoader.getResource("classpath:data/crime-nyc.csv");
        InputStream inputStream = csvResource.getInputStream();

        Source source = sourceRepository.findOneBySourceConfigId(12);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, 0, 0, "id");

        String filename = "test-" + dataJob.buildFilename();
        String filePath = String.join("/", FETCH_DATA_DIR, filename);

        Path path = Paths.get(FETCH_DATA_DIR);
        when(fileStoreProvider.getFileStore(path)).thenReturn(fs);
        when(fs.getUsableSpace()).thenReturn(DATA_DIR_MIN_FREE + 1);
        when(mockDataJob.getSource()).thenReturn(source);
        when(mockDataJob.buildFilename()).thenReturn(filename);

        File file = new File(filePath);

        DataParser dataParser = dataParserFactory
                .getDataParser(source.getDataParserType());

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        fileDataProcessStrategy.process(mockDataJob, inputStream, dataParser);

        assertThat(dataCrimeRepository.count()).isEqualTo(10);

        assertThat(file.exists()).isTrue();

        if (file.exists()) {
            file.delete();
        }
    }

    // test quoted strings with interal comma, ensure record is parsed accordingly.
    @Test
    @Transactional
    public void processQuotes311NYCTest() throws IOException {

        Resource csvResource = resourceLoader.getResource("classpath:data/quotes.csv");
        InputStream inputStream = csvResource.getInputStream();

        Source source = sourceRepository.findOneBySourceConfigId(14);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, 0, 0, "id");

        String filename = "test-" + dataJob.buildFilename();
        String filePath = String.join("/", FETCH_DATA_DIR, filename);

        Path path = Paths.get(FETCH_DATA_DIR);
        when(fileStoreProvider.getFileStore(path)).thenReturn(fs);
        when(fs.getUsableSpace()).thenReturn(DATA_DIR_MIN_FREE + 1);
        when(mockDataJob.getSource()).thenReturn(source);
        when(mockDataJob.buildFilename()).thenReturn(filename);

        File file = new File(filePath);

        DataParser dataParser = dataParserFactory
                .getDataParser(source.getDataParserType());

        assertThat(data311Repository.count()).isEqualTo(0);

        fileDataProcessStrategy.process(mockDataJob, inputStream, dataParser);

        assertThat(data311Repository.count()).isEqualTo(20);

        List<String> reportIds = new ArrayList<String>();
        reportIds.add("16236266");
        reportIds.add("16236267");
        List<Data311> data311s = data311Repository
            .findAllBySourceIdAndReportNumIn(source.getId(), reportIds);

        assertThat(file.exists()).isTrue();

        if (file.exists()) {
            file.delete();
        }
    }

}
