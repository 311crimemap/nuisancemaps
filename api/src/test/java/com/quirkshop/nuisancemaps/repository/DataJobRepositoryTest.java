package com.quirkshop.nuisancemaps.repository;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobRepositoryTest {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    public SourceRepository sourceRepository;

    @Test
    @Transactional
    public void findLastDataJobBySourceTest() {
        Source source = new Source("category", "description", "url");
        Source source2 = new Source("category", "description", "url");
        sourceRepository.save(source);
        sourceRepository.save(source2);

        DataJob datajob = new DataJob(source, 0, 0, "id");
        DataJob datajob2 = new DataJob(source2, 0, 0, "id");
        dataJobRepository.save(datajob);
        dataJobRepository.save(datajob2);

        DataJob res = dataJobRepository.findLastDataJobBySource(source.getId());
        assertThat(res.getId()).isEqualTo(datajob.getId());
    }

    @Test
    @Transactional
    public void findTopBySourceIdOrderByOffsetDescTest() {
        Source source = new Source("category", "description", "url");
        Source source2 = new Source("category", "description", "url");
        sourceRepository.save(source);
        sourceRepository.save(source2);

        DataJob datajob = new DataJob(source, 0, 0, "id");
        DataJob datajob2 = new DataJob(source2, 0, 1000, "id");
        dataJobRepository.save(datajob);
        dataJobRepository.save(datajob2);

        DataJob res = dataJobRepository.findTopBySourceIdOrderByParamOffsetDesc(source2.getId());
        assertThat(res.getId()).isEqualTo(datajob2.getId());
    }

}
