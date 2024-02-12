package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import com.quirkshop.nuisancemaps.model.Test;

public interface TestRepository extends CrudRepository<Test, Integer> {
    // auto implemented
}
