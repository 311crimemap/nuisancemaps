package com.quirkshop.nuisancemaps.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.model.Source;

import jakarta.persistence.EntityManager;

interface SourceCustomRepository {
        Source findOrCreate(Source source);
}

class SourceCustomRepositoryImpl implements SourceCustomRepository {

        @Autowired
        private EntityManager entityManager;

        @Transactional
        public Source findOrCreate(Source source) {

                Source s = entityManager
                                .createQuery("SELECT s FROM Source s WHERE s.url = :value1", Source.class)
                                .setParameter("value1", source.getUrl())
                                .setMaxResults(1)
                                .getResultList()
                                .stream()
                                .findFirst()
                                .orElse(null);
                if (s == null) {
                        entityManager.persist(source);
                        return source;
                }

                return s;
        }
}

@Repository
public interface SourceRepository extends CrudRepository<Source, Integer>, SourceCustomRepository {
        // auto implemented
}
