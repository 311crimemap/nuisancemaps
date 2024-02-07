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

    default DataJob createNewDataJob(Source source, DataJob prevDataJob, Integer PARAM_LIMIT)
            throws UnsupportedEncodingException {
        String key = source.getMapping().get("report_num").toString();
        DataJob dataJob;

        if (prevDataJob == null) {
            dataJob = new DataJob(source, PARAM_LIMIT, 0, key);
        } else {
            dataJob = new DataJob(source,
                    PARAM_LIMIT,
                    prevDataJob.getParamOffset() + PARAM_LIMIT,
                    prevDataJob.getOrderKey());
        }

        dataJob.buildURL();
        save(dataJob);
        return dataJob;
    }

    @Transactional
    default DataJob createNextDataJob(Source source, Integer PARAM_LIMIT) throws UnsupportedEncodingException {
        //NB: Locked
        DataJob maxOffsetDataJob = findTopBySourceIdOrderByParamOffsetDesc(source.getId());

        // New Source job offset: 0
        if (maxOffsetDataJob == null) {
            DataJob newJob = createNewDataJob(source, null, PARAM_LIMIT);
            return newJob;
        }

        // Next job
        if (maxOffsetDataJob.getParamOffset() + PARAM_LIMIT < source.getNumRecords()) {
            DataJob nextJob = createNewDataJob(source, maxOffsetDataJob, PARAM_LIMIT);
            return nextJob;
        }

        // all caught up - no new jobs
        return null;
    }

    @Query("SELECT d from DataJob d WHERE d.source.id = ?1 AND (d.status = DataJobStatus.COMPLETED OR d.status = DataJobStatus.QUEUED) ORDER BY id DESC LIMIT 1")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataJob findLastDataJobBySource(Integer source_id);

    @Transactional
    default DataJob createLastDataJobBySource(Source source, Integer PARAM_LIMIT) throws UnsupportedEncodingException {
        // lock
        DataJob lastDataJob = findLastDataJobBySource(source.getId());

        // start from scratch initial crawl
        if (lastDataJob == null) {
            DataJob newJob = createNewDataJob(source, null, PARAM_LIMIT);
            return newJob;
        }

        // if found last "completed" job; we create a new job from that offset
        if (lastDataJob.getStatus().equals(DataJobStatus.COMPLETED)) {
            DataJob nextJob = createNewDataJob(source, lastDataJob, PARAM_LIMIT);
            return nextJob;
        }

        // if last job is "queued" leave it as-is, to be picked up by scheduled task
        lastDataJob.setStatus(DataJobStatus.QUEUED);
        save(lastDataJob);
        return lastDataJob;
    }
}
