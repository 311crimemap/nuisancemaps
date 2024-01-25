package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.quirkshop.nuisancemaps.model.Data311;

@Repository
public interface Data311Repository extends CrudRepository<Data311, Integer> {
    // auto implemented
}
