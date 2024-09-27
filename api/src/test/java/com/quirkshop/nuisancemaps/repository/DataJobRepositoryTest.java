package com.quirkshop.nuisancemaps.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.model.datajob.DataJobConfiguratorType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobRepositoryTest {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    public LocaleRepository localeRepository;

    @Autowired
    public MappingRepository mappingRepository;

    @Autowired
    public SourceRepository sourceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Mapping mapping;
    private Mapping mapping2;
    private Source source;
    private Source source2;


    @BeforeEach
    public void setUp() {
        Locale locale = new Locale();
        localeRepository.save(locale);
        mapping = new Mapping();
        mapping2 = new Mapping();
        mappingRepository.save(mapping);
        mappingRepository.save(mapping2);
        source = new Source(locale, "category", "description", "url",
                            DataParserType.JSON, DataProcessType.MEMORY, DataJobConfiguratorType.BASE);
        source2 = new Source(locale, "category", "description", "url",
                             DataParserType.JSON, DataProcessType.MEMORY, DataJobConfiguratorType.BASE);
        source.setMapping(mapping);
        source2.setMapping(mapping2);
        sourceRepository.save(source);
        sourceRepository.save(source2);
    }


    @Test
    @Transactional
    public void findLastDataJobBySourceTest() {

        DataJob datajob = new DataJob(LocalDateTime.now(), source, "id");
        DataJob datajob2 = new DataJob(LocalDateTime.now(), source2, "id");
        dataJobRepository.save(datajob);
        dataJobRepository.save(datajob2);

        DataJob res = dataJobRepository.findLastDataJobBySource(source.getId());
        assertThat(res.getId()).isEqualTo(datajob.getId());
    }

    @Test
    @Transactional
    public void findTopBySourceIdOrderByParamOffsetDescTest() {

        DataJob datajob = new DataJob(LocalDateTime.now(), source, "id");
        DataJob datajob2 = new DataJob(LocalDateTime.now(), source2, "id");
        datajob2.setParamOffset(1000);
        dataJobRepository.save(datajob);
        dataJobRepository.save(datajob2);

        DataJob res = dataJobRepository.findTopBySourceIdOrderByParamOffsetDesc(source2.getId());
        assertThat(res.getId()).isEqualTo(datajob2.getId());
    }

    @Test
    @Transactional
    public void findTopBySourceIdOrderBySessionIdDescParamOffsetDescTest() {

        LocalDateTime sessionId = LocalDateTime.now();
        DataJob datajob = new DataJob(sessionId, source, "id");
        DataJob datajob2 = new DataJob(sessionId, source, "id");
        datajob2.setParamOffset(100);
        DataJob datajob3 = new DataJob(sessionId.plusDays(1), source, "id");
        datajob3.setParamOffset(200);

        DataJob datajob4 = new DataJob(sessionId.minusDays(1), source2, "id");
        datajob4.setParamOffset(1000);
        DataJob datajob5 = new DataJob(sessionId.minusDays(1), source2, "id");
        datajob5.setParamOffset(2000);
        DataJob datajob6 = new DataJob(sessionId, source2, "id");
        datajob6.setParamOffset(1000);

        dataJobRepository.save(datajob);
        dataJobRepository.save(datajob2);
        dataJobRepository.save(datajob3);
        dataJobRepository.save(datajob4);
        dataJobRepository.save(datajob5);
        dataJobRepository.save(datajob6);

        DataJob res = dataJobRepository.findTopBySourceIdOrderBySessionIdDescParamOffsetDesc(source.getId());
        assertThat(res.getId()).isEqualTo(datajob3.getId());

        DataJob res2 = dataJobRepository.findTopBySourceIdOrderBySessionIdDescParamOffsetDesc(source2.getId());
        assertThat(res2.getId()).isEqualTo(datajob6.getId());

    }

    @Test
    @Transactional
    public void updateAllIncompleteBeforeTest() {

        DataJob datajob1 = new DataJob(LocalDateTime.now(), source, "id");
        DataJob datajob2 = new DataJob(LocalDateTime.now(), source, "id");
        datajob2.setParamOffset(1000);
        DataJob datajob3 = new DataJob(LocalDateTime.now(), source, "id");
        datajob3.setParamOffset(2000);
        DataJob datajob4 = new DataJob(LocalDateTime.now(), source, "id");
        datajob4.setParamOffset(3000);
        DataJob datajob5 = new DataJob(LocalDateTime.now(), source, "id");
        datajob5.setParamOffset(4000);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime aDayAgo = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);

        // datajob1: queued & new
        datajob1.setStatus(DataJobStatus.QUEUED);
        datajob1.setUpdatedAt(now);

        datajob2.setUpdatedAt(aDayAgo.minusSeconds(60));
        datajob2.setStatus(DataJobStatus.FETCH_START); // orphaned job a day ago

        datajob3.setUpdatedAt(twoDaysAgo);
        datajob3.setStatus(DataJobStatus.QUEUED); // too old job and queued status

        datajob4.setUpdatedAt(now);
        datajob4.setStatus(DataJobStatus.ERROR); // error and recent

        datajob5.setUpdatedAt(twoDaysAgo);
        datajob5.setStatus(DataJobStatus.ERROR); // error but too old

        // save
        dataJobRepository.save(datajob1);
        dataJobRepository.save(datajob2);
        dataJobRepository.save(datajob3);
        dataJobRepository.save(datajob4);
        dataJobRepository.save(datajob5);

        assertThat(dataJobRepository.count()).isEqualTo(5);

        // NB: hiberate applies updated_at timestamp on save automatically
        // need a native SQL query to manually change and persist updated_at
        //
        entityManager.createNativeQuery("UPDATE data_job SET updated_at = :updatedAt WHERE id = :id")
                .setParameter("updatedAt", twoDaysAgo)
                .setParameter("id", datajob5.getId())
                .executeUpdate();

        // datajob1 - newly queued: no restart
        // datajob2; - orphaned within a day: restartable
        // datajob3: - too old and queued: no restart
        // datajob4: - error but new: restartable
        // datajob5: - error but too old: no restart
        //
        // so no queued or complete
        // and updated no less than cutoff - (aDayAgo)
        List<DataJobStatus> excludedStatuses = Arrays.asList(DataJobStatus.COMPLETED, DataJobStatus.QUEUED);

        int num = dataJobRepository.updateAllIncompleteToQueuedBefore(DataJobStatus.QUEUED, LocalDateTime.now(),
                excludedStatuses, aDayAgo);


        assertThat(num).isEqualTo(2);
    }
}
