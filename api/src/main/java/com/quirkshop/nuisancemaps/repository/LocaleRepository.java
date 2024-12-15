package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Locale;

@Repository
public interface LocaleRepository extends CrudRepository<Locale, Integer> {

    @Query("SELECT l FROM Locale l LEFT JOIN FETCH l.sources")
    List<Locale> findAllWithSources();
}
