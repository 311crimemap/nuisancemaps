package com.quirkshop.nuisancemaps.repository;

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

    @Test
    @Transactional
    public void updateAllIncompleteBeforeTest() {

        Source source = new Source("category", "description", "url");
        sourceRepository.save(source);

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
