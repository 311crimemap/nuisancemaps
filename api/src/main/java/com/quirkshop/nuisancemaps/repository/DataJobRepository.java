package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.quirkshop.nuisancemaps.model.DataJob;

@Repository
public interface DataJobRepository extends CrudRepository<DataJob, Integer> {

}
