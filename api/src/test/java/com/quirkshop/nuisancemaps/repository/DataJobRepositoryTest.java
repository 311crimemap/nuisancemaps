package com.quirkshop.nuisancemaps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobRepositoryTest {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    public MappingRepository mappingRepository;

    @Autowired
    public SourceRepository sourceRepository;

    private Mapping mapping;
    private Mapping mapping2;
    private Source source;
    private Source source2;


    @BeforeEach
    public void setUp() {
        mapping = new Mapping();
        mapping2 = new Mapping();
        mappingRepository.save(mapping);
        mappingRepository.save(mapping2);
        source = new Source("category", "description", "url");
        source2 = new Source("category", "description", "url");
        source.setMapping(mapping);
        source2.setMapping(mapping2);
        sourceRepository.save(source);
        sourceRepository.save(source2);
    }


    @Test
    @Transactional
    public void findLastDataJobBySourceTest() {

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

        DataJob datajob = new DataJob(source, 0, 0, "id");
        DataJob datajob2 = new DataJob(source2, 0, 1000, "id");
        dataJobRepository.save(datajob);
        dataJobRepository.save(datajob2);

        DataJob res = dataJobRepository.findTopBySourceIdOrderByParamOffsetDesc(source2.getId());
        assertThat(res.getId()).isEqualTo(datajob2.getId());
    }

    @Test
    @Transactional
    public void updateAllIncompleteBeforeTest() {

        DataJob datajob1 = new DataJob(source, 0, 0, "id");
        DataJob datajob2 = new DataJob(source, 0, 1000, "id");
        DataJob datajob3 = new DataJob(source, 0, 2000, "id");
        DataJob datajob4 = new DataJob(source, 0, 3000, "id");
        DataJob datajob5 = new DataJob(source, 0, 4000, "id");

        LocalDateTime aDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime TwoDaysAgo = LocalDateTime.now().minusDays(2);

        // datajob1: queued & new

        datajob2.setUpdatedAt(aDayAgo);
        datajob2.setStatus(DataJobStatus.FETCH_START); // orphaned job a day ago

        datajob3.setUpdatedAt(TwoDaysAgo);
        datajob3.setStatus(DataJobStatus.QUEUED); // old job but still queued status

        datajob4.setStatus(DataJobStatus.ERROR); // error but new

        datajob5.setUpdatedAt(TwoDaysAgo);
        datajob5.setStatus(DataJobStatus.ERROR); // error but old (restartable)

        dataJobRepository.save(datajob1);
        dataJobRepository.save(datajob2);
        dataJobRepository.save(datajob3);
        dataJobRepository.save(datajob4);
        dataJobRepository.save(datajob5);

        // datajob1 - newly queued: no restart
        // datajob2; - old & orphaned: restartable
        // datajob3: - old but still queued: no restart
        // datajob4: - error but new: no restart
        // datajob5: - error but old: restart

        List<DataJobStatus> excludedStatuses = Arrays.asList(DataJobStatus.COMPLETED, DataJobStatus.QUEUED);
        int num = dataJobRepository.updateAllIncompleteToQueuedBefore(DataJobStatus.QUEUED, LocalDateTime.now(),
                excludedStatuses, aDayAgo);
        assertThat(num).isEqualTo(2);
    }
}
