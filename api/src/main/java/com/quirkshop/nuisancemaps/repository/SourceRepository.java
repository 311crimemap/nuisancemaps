package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.model.Source;

@Repository
public interface SourceRepository extends CrudRepository<Source, Integer> {

        // auto implemented
        public Source findOneByUrl(String url);

        @Transactional
        default Source findOrCreate(Source source) {
                Source s = findOneByUrl(source.getUrl());
                if (s != null) {
                        return s;
                }
                s = save(source);
                return s;
        }
}
