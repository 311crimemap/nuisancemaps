package com.quirkshop.nuisancemaps.service.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.dataprocess.FileDataProcessStrategy;
import com.quirkshop.nuisancemaps.service.dataprocess.FileStoreProvider;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;

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

    @InjectMocks
    FileDataProcessStrategy fileDataProcessStrategy;

    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");

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

    @Test
    @Transactional
    public void validDiskSpaceTest() throws IOException {

        long DATA_DIR_MIN_FREE = Long.parseLong(System.getenv("DATA_DIR_MIN_FREE"));

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
        File file = new File(String.join("/", FETCH_DATA_DIR, filename));

        if (file.exists()) {
            file.delete();
        }

        fileDataProcessStrategy.writeToFile(filename, inputStream);

        assertThat(file.exists()).isTrue();
        assertThat(file.isFile()).isTrue();

        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @Transactional
    public void process() throws IOException {
        /*
         * Resource jsonResource =
         * resourceLoader.getResource("classpath:data/crime-nyc.csv");
         * InputStream inputstream = jsonResource.getInputStream();
         * ParseCounter parseCounter = new ParseCounter();
         * 
         * Source s = sourceRepository.findOneBySourceConfigId(12);
         * DataJob d = new DataJob(LocalDateTime.now(), s, 1000, 100, "CMPLNT_NUM");
         * dataJobRepository.save(d);
         */
    }

}
