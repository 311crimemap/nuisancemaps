package com.quirkshop.repository;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

//@SpringBootTest
@SpringBootTest(classes = NuisancemapsApplication.class)
public class SourceRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @Autowired
    public SourceRepository srepo;

    @Test
    public void SourceRepositoryFindOrCreate() throws Exception {
        Source s = new Source("category", "description", "url");
        assertThat(s.getId()).isNull();
        s = srepo.findOrCreate(s);
        assertThat(s.getId()).isNotNull();
        Source t = srepo.findOrCreate(s);
        assertThat(s.getId()).isEqualTo(t.getId());
    }
}
