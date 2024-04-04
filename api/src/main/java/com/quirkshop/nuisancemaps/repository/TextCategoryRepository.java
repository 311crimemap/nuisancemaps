package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.TextCategory;

public interface TextCategoryRepository extends CrudRepository<TextCategory, Integer> {

    public TextCategory findByDataTypeAndText(String dataType, String text);
}
