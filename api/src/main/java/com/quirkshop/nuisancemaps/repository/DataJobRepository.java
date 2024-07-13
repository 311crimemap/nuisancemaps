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

    /* return highest offset job from most recent session per source */
    DataJob findTopBySourceIdOrderBySessionIdDescParamOffsetDesc(Integer sourceId);

    DataJob findTopBySourceIdOrderByParamOffsetDesc(Integer source_id);

    DataJob findTopByStatusOrderByIdAsc(DataJobStatus status);

    // "earliest" QUEUED job (regardless of source or session)
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

        String key = source.getMapping().getOrderKey(); // NB: prevDataJob might exist
        DataJob dataJob;

        if (prevDataJob == null) {
            // start new 'crawl' session
            dataJob = new DataJob(LocalDateTime.now(), source, paramLimit, paramOffset, key);
        } else {
            // next offset in same session
            dataJob = new DataJob(prevDataJob.getSessionId(),
                    source,
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
        DataJob maxSessionIdOffsetDataJob = findTopBySourceIdOrderBySessionIdDescParamOffsetDesc(source.getId());

        // no job for source has ever existed, start fresh 0
        if (maxSessionIdOffsetDataJob == null) {
            DataJob newJob = createNewDataJob(source, paramLimit, 0, null);
            return newJob;
        }

        // numFetched null: have a Source DataJob but yet to fetch, or in mid-fetch
        // we can wait until next round
        if (maxSessionIdOffsetDataJob.getNumFetched() == null)
            return null;

        // != 0 - has fetched so continue fetching next set until we get 0 - know for
        // sure we've reached the end.
        if (maxSessionIdOffsetDataJob.getNumFetched() != 0) {
            DataJob nextJob = createNewDataJob(source,
                    paramLimit,
                    maxSessionIdOffsetDataJob.getParamOffset() + paramLimit,
                    maxSessionIdOffsetDataJob);
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
        // updated or new records in that range, they will be ingested.
        // use case restarted worker
        DataJob nextJob = createNewDataJob(source,
                paramLimit,
                lastDataJob.getParamLimit(),
                lastDataJob);
        return nextJob;
    }
}
