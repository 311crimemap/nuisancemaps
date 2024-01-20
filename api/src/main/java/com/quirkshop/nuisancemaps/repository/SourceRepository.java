package com.quirkshop.nuisancemaps.repository;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
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

                Optional<Source> optionalSource = Optional.ofNullable(
                                entityManager.find(Source.class, source.getId() == null ? -1 : source.getId()));
                return optionalSource.orElseGet(() -> {
                        entityManager.persist(source); // Save the new entity
                        return source;
                });
        }
}

public interface SourceRepository extends CrudRepository<Source, Integer>, SourceCustomRepository {
        // auto implemented
}
