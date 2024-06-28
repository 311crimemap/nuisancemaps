package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.quirkshop.nuisancemaps.model.DataError;

@Repository
public interface DataErrorRepository extends CrudRepository<DataError, Integer> {

    List<DataError> findAll();

    List<DataError> findAllByOrderByUpdatedAtDesc(PageRequest n);
}
