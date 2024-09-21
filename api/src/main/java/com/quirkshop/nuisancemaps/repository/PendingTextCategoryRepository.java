package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.quirkshop.nuisancemaps.model.PendingTextCategory;

@Repository
public interface PendingTextCategoryRepository extends CrudRepository<PendingTextCategory, Integer> {
    List<PendingTextCategory> findByDataTypeAndTextIn(String dataType, List<String> texts);

    List<PendingTextCategory> findAllByOrderByIdDesc();

    List<PendingTextCategory> findAllByDataTypeOrderByIdDesc(String dataType);

    @Transactional
    void deleteByDataTypeAndText(String dataType, String text);
}
