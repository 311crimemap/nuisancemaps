package com.quirkshop.nuisancemaps.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SourceRepositoryTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    public MappingRepository mappingRepository;

    @Autowired
    public SourceRepository sourceRepository;

    @Autowired
    Environment env;

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

    @Test
    @Transactional
    public void SourceRepositoryFindOrCreate() throws Exception {
        Mapping m = new Mapping();
        Mapping m2 = new Mapping();
        mappingRepository.save(m);
        mappingRepository.save(m2);

        Source s = new Source("category", "description", "url");
        s.setMapping(m);

        assertThat(s.getId()).isNull();
        s = sourceRepository.findOrCreate(s);
        assertThat(s.getId()).isNotNull();

        Source t = sourceRepository.findOrCreate(s);
        assertThat(s.getId()).isEqualTo(t.getId());

        Source x = new Source("category2", "description2", "url2");
        x.setMapping(m2);
        Source y = sourceRepository.findOrCreate(x);
        assertThat(y.getId()).isNotEqualTo(s.getId());

        Source z = new Source("category2", "description2", "url");
        Source a = sourceRepository.findOrCreate(z);
        assertThat(a.getId()).isEqualTo(s.getId());
    }

    @Test
    @Transactional
    public void findBySourceConfigIDTest() {

        Source s = sourceRepository.findOneBySourceConfigId(1);
        assertThat(s.getSourceConfigId()).isEqualTo(1);

        //ensure mapping is intact
        assertThat(s).isInstanceOf(Source.class);
        Mapping m = s.getMapping();
        assertThat(m.getReportNum()).isEqualTo("/incident_report_number");
    }

}
