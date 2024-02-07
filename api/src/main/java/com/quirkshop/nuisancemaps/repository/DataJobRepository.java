package com.quirkshop.nuisancemaps.repository;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;

@Repository
public interface DataJobRepository extends CrudRepository<DataJob, Integer> {

    List<DataJob> findAll();

    List<DataJob> findAllByOrderByIdDesc();

    List<DataJob> findAllByOrderByIdDesc(PageRequest n);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataJob findTopBySourceIdOrderByParamOffsetDesc(Integer source_id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataJob findTopByStatusOrderByIdAsc(DataJobStatus status);

    @Transactional
    default DataJob getNextDataJob(DataJobStatus status) {
        DataJob dataJob = findTopByStatusOrderByIdAsc(status);
        if (dataJob == null)
            return null;
        dataJob.setStatus(DataJobStatus.START);
        dataJob = save(dataJob);
        return dataJob;
    }

    @Transactional
    default DataJob createNextDataJob(Source source, Integer PARAM_LIMIT) throws UnsupportedEncodingException {
        //NB: Locked
        DataJob maxOffsetDataJob = findTopBySourceIdOrderByParamOffsetDesc(source.getId());

        // New Source job offset: 0
        if (maxOffsetDataJob == null) {
            String key = source.getMapping().get("report_num").toString();
            DataJob initDatajob = new DataJob(source, PARAM_LIMIT, 0, key);
            save(initDatajob);
            return initDatajob;
        }

        // Next job
        if (maxOffsetDataJob.getParamOffset() + PARAM_LIMIT < source.getNumRecords()) {

            DataJob nextJob = new DataJob(source,
                                          PARAM_LIMIT,
                                          maxOffsetDataJob.getParamOffset() + PARAM_LIMIT,
                                          maxOffsetDataJob.getOrderKey());

            nextJob.buildURL();
            nextJob.setStatus(DataJobStatus.QUEUED);
            save(nextJob);
            return nextJob;
        }

        // all caught up - no new jobs
        return null;
    }

    @Query("SELECT d from DataJob d WHERE d.source.id = ?1 AND (d.status = DataJobStatus.COMPLETED OR d.status = DataJobStatus.QUEUED) ORDER BY id DESC LIMIT 1")
    DataJob findLastDataJobBySource(Integer source_id);

}
