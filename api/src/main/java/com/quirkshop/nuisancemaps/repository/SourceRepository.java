package com.quirkshop.nuisancemaps.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

@Repository
public interface SourceRepository extends CrudRepository<Source, Integer> {

    public Source findOneByUrl(String url);

    public Source findOneBySourceConfigId(Integer id);

    @EntityGraph(attributePaths = { "locale", "mapping" })
    public List<Source> findAll();

    public List<Source> findAllByLocaleId(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Source findByIdAndUpdatedAtBefore(Integer id, LocalDateTime localDateTime);

    @Transactional
    default boolean needsUpdateAndTouch(Source source, LocalDateTime nowMinusHours) {
        // lock
        Source s = findByIdAndUpdatedAtBefore(source.getId(), nowMinusHours);
        if (s == null)
            return false;
        source.setUpdatedAt(LocalDateTime.now());
        save(source);
        return true;
    }
}
