package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

import com.quirkshop.nuisancemaps.model.Source;

@Repository
public interface SourceRepository extends CrudRepository<Source, Integer> {

    // auto implemented
    public Source findOneByUrl(String url);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    default Source findOrCreate(Source source) {
        Source s = findOneByUrl(source.getUrl());
        if (s != null) {
            return s;
        }
        s = save(source);
        return s;
    }

}
