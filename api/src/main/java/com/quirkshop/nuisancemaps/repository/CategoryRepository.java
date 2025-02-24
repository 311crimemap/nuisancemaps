package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Category;

import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Integer> {

    List<Category> findAllByDataType(String dataType);

    List<Category> findAllByText(String text);

    List<Category> findAllByOrderByIdAsc();

    List<Category> findAllByTextNotOrderByIdAsc(String text);

    Category findByDataTypeAndTextAndLabel(String dataType, String text, Integer label);
}
