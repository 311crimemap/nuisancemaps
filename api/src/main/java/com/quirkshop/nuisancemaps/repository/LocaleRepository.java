package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.quirkshop.nuisancemaps.model.Locale;

@Repository
public interface LocaleRepository extends CrudRepository<Locale, Integer> {

}
