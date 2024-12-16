package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.LocaleCategoryMinMaxReportedAt;

@Repository
public interface LocaleCategoryMinMaxReportedAtRepository
        extends CrudRepository<LocaleCategoryMinMaxReportedAt, Integer> {

    @Query(value = "SELECT * FROM locale_category_min_max_reported_at ORDER BY locale_id, category", nativeQuery = true)
    List<LocaleCategoryMinMaxReportedAt> findAll();

    @Modifying
    @Query(value = "REFRESH MATERIALIZED VIEW locale_category_min_max_reported_at", nativeQuery = true)
    void refreshMaterializedView();
}
