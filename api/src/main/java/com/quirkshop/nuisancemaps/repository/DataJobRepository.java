package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;

import jakarta.persistence.EntityManager;

interface DataJobCustomRepository {
    DataJob findLastDataJobBySource(Source source);

    DataJob getNextDataJob(DataJobStatus status);
}

class DataJobCustomRepositoryImpl implements DataJobCustomRepository {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public DataJob findLastDataJobBySource(Source source) {

        // NB: this is JPQL so you use the entity name (not table name)
        List<DataJob> resultList = entityManager
                .createQuery(
                        "SELECT d FROM DataJob d WHERE d.source.id = :sourceId AND (d.status = :status_completed OR d.status = :status_queued) ORDER BY d.id DESC",
                        DataJob.class)
                .setParameter("sourceId", source.getId())
                .setParameter("status_completed", DataJobStatus.COMPLETED)
                .setParameter("status_queued", DataJobStatus.QUEUED)
                .setMaxResults(1)
                .getResultList();

        if (!resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }

    @Transactional
    public DataJob getNextDataJob(DataJobStatus status) {
        List<DataJob> resultList = entityManager
                .createQuery(
                        "SELECT d FROM DataJob d WHERE d.status = :status ORDER BY d.id ASC",
                        DataJob.class)
                .setParameter("status", status)
                .setMaxResults(1)
                .getResultList();

        if (!resultList.isEmpty()) {
            return resultList.get(0);
        }

        return null;
    }
}

@Repository
public interface DataJobRepository extends CrudRepository<DataJob, Integer>, DataJobCustomRepository {

}