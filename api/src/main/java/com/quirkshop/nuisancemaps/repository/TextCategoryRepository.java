package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.TextCategory;

public interface TextCategoryRepository extends CrudRepository<TextCategory, Integer> {

    public List<TextCategory> findAllByOrderByCreatedAtDesc(PageRequest n);

    public TextCategory findByDataTypeAndText(String dataType, String text);
}
