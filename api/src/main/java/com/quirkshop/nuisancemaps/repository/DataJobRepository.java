package com.quirkshop.nuisancemaps.repository;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    List<DataJob> findAllByOrderByUpdatedAtDesc(PageRequest n);

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

    default DataJob createNewDataJob(Source source, Integer paramLimit, Integer paramOffset, DataJob prevDataJob)
            throws UnsupportedEncodingException {

        String key = source.getMapping().getReportNum(); // NB: prevDataJob might exist
        DataJob dataJob;

        if (prevDataJob == null) {
            dataJob = new DataJob(source, paramLimit, paramOffset, key);
        } else {
            dataJob = new DataJob(source,
                    paramLimit,
                    paramOffset,
                    prevDataJob.getOrderKey());
        }

        dataJob.buildURL();
        save(dataJob);
        return dataJob;
    }

    @Transactional
    default DataJob createNextDataJob(Source source, Integer paramLimit) throws UnsupportedEncodingException {
        // NB: Locked
        DataJob maxOffsetDataJob = findTopBySourceIdOrderByParamOffsetDesc(source.getId());

        // New Source job offset: 0
        if (maxOffsetDataJob == null) {
            DataJob newJob = createNewDataJob(source, paramLimit, 0, null);
            return newJob;
        }

        // Create next job
        if (maxOffsetDataJob.getParamOffset() + paramLimit < source.getNumRecords()) {
            DataJob nextJob = createNewDataJob(source,
                    paramLimit,
                    maxOffsetDataJob.getParamOffset() + paramLimit,
                    maxOffsetDataJob);
            return nextJob;
        }

        // all caught up - no new jobs
        return null;
    }

    // NB: JPQL doesn't support enums as params
    // but is allowed in queries (e.g. where)
    @Transactional
    @Modifying
    @Query("UPDATE DataJob SET status = :status, updatedAt = :updatedAt WHERE status NOT IN :statuses AND updatedAt <= :cutOffTime")
    int updateAllIncompleteToQueuedBefore(DataJobStatus status, LocalDateTime updatedAt,
            List<DataJobStatus> statuses, LocalDateTime cutOffTime);

    @Query("SELECT d from DataJob d WHERE d.source.id = ?1 AND (d.status = DataJobStatus.COMPLETED OR d.status = DataJobStatus.QUEUED) ORDER BY id DESC LIMIT 1")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataJob findLastDataJobBySource(Integer source_id);

    @Transactional
    default DataJob createLastDataJobBySource(Source source, Integer paramLimit, LocalDateTime cutOffTime)
            throws UnsupportedEncodingException {

        // NB: we don't want only the last COMPLETED job, otherwise we might
        // repeatedly create duplicates of an existing next QUEUED job

        // lock
        DataJob lastDataJob = findLastDataJobBySource(source.getId());

        // start from scratch initial crawl
        if (lastDataJob == null) {
            DataJob newJob = createNewDataJob(source, paramLimit, 0, null);
            return newJob;
        }

        // last job is status "QUEUED" so don't create new tasks, leave everything
        // as-is, to be picked up by scheduled task
        if (lastDataJob.getStatus().equals(DataJobStatus.QUEUED)) {
            return null;
        }

        // prevent duplicate jobs; if last job was created too recently, exit
        // (e.g. multiple workers)
        if (cutOffTime.isBefore(lastDataJob.getCreatedAt())) {
            return null;
        }

        // If found last "COMPLETED" job; we create a copy of that job.
        // Our goal is to effectively "redo" the completed job. If there are
        // updated or new records in that range, they will ingested.
        DataJob nextJob = createNewDataJob(source,
                paramLimit,
                lastDataJob.getParamLimit(),
                lastDataJob);
        return nextJob;
    }
}
