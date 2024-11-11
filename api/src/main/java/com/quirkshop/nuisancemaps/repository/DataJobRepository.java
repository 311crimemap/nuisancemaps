package com.quirkshop.nuisancemaps.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

@Repository
public interface DataJobRepository extends CrudRepository<DataJob, Integer> {

    List<DataJob> findAll();

    List<DataJob> findAllByOrderByIdDesc();

    List<DataJob> findAllByOrderByIdDesc(PageRequest n);

    List<DataJob> findAllByOrderByUpdatedAtDesc(PageRequest n);

    /* return highest offset job from most recent session per source */
    DataJob findTopBySourceIdOrderBySessionIdDescParamOffsetDescIdDesc(Integer sourceId);

    DataJob findTopBySourceIdOrderBySessionIdDescParamOffsetDesc(Integer sourceId);

    DataJob findTopBySourceIdOrderByParamOffsetDesc(Integer source_id);

    DataJob findTopByStatusOrderByIdAsc(DataJobStatus status);

    // inner: find max data job via group by, then match return data_job d.* for JPA
    @Query(value = "SELECT d.* " +
            "FROM data_job d " +
            "WHERE (d.source_id, d.session_id, d.param_offset, d.id) IN ( " +
            "    SELECT source_id, MAX(session_id), MAX(param_offset), MAX(id) " +
            "    FROM data_job " +
            "    GROUP BY source_id " +
            ") " +
            "ORDER BY d.source_id;", nativeQuery = true)
    List<DataJob> findMaxSessionIdOffsetDataJobs();

    // NB: JPQL doesn't support enums as params
    // but is allowed in queries (e.g. where)
    @Transactional
    @Modifying
    @Query("UPDATE DataJob SET status = :status, updatedAt = :updatedAt WHERE status NOT IN :statuses AND updatedAt >= :cutOffTime")
    int updateAllIncompleteToQueuedBefore(DataJobStatus status, LocalDateTime updatedAt,
            List<DataJobStatus> statuses, LocalDateTime cutOffTime);

    @Transactional
    @Modifying
    @Query("UPDATE DataJob SET status = :status, updatedAt = :updatedAt WHERE status IN :statuses AND updatedAt >= :cutOffTime")
    int updateAllErrorsToQueuedBefore(DataJobStatus status, LocalDateTime updatedAt,
            List<DataJobStatus> statuses, LocalDateTime cutOffTime);

    @Query("SELECT d from DataJob d WHERE d.status IN :statuses ORDER BY id DESC")
    List<DataJob> findAllInStatuses(List<DataJobStatus> statuses);

    @Query("SELECT d from DataJob d WHERE d.source.id = ?1 AND (d.status = DataJobStatus.COMPLETED OR d.status = DataJobStatus.QUEUED) ORDER BY id DESC LIMIT 1")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataJob findLastDataJobBySource(Integer source_id);

}
