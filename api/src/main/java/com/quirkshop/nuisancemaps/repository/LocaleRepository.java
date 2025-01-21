package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Locale;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocaleRepository extends CrudRepository<Locale, Integer> {
    List<Locale> findAll();
}
