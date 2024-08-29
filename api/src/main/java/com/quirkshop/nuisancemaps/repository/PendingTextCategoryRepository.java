package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.quirkshop.nuisancemaps.model.PendingTextCategory;

@Repository
public interface PendingTextCategoryRepository extends CrudRepository<PendingTextCategory, Integer> {

}
