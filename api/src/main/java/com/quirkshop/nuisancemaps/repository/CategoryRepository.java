package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import com.quirkshop.nuisancemaps.model.Category;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
    // auto implemented

    List<Category> findAllByDataType(String dataType);

    List<Category> findAllByText(String text);

    List<Category> findAllByOrderByIdAsc();

    Category findByDataTypeAndTextAndLabel(String dataType, String text, Integer label);
}
