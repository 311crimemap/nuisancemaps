package com.quirkshop.repository;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

//@SpringBootTest
@SpringBootTest(classes = NuisancemapsApplication.class)
public class SourceRepositoryTest {

    @Autowired
    public SourceRepository srepo;

    @Autowired
    Environment env;

    @Test
    @Transactional
    public void SourceRepositoryFindOrCreate() throws Exception {
        Source s = new Source("category", "description", "url");
        assertThat(s.getId()).isNull();
        s = srepo.findOrCreate(s);
        assertThat(s.getId()).isNotNull();

        Source t = srepo.findOrCreate(s);
        assertThat(s.getId()).isEqualTo(t.getId());

        Source x = new Source("category2", "description2", "url2");
        Source y = srepo.findOrCreate(x);
        assertThat(y.getId()).isNotEqualTo(s.getId());

        Source z = new Source("category2", "description2", "url");
        Source a = srepo.findOrCreate(z);
        assertThat(a.getId()).isEqualTo(s.getId());
    }
}
