package com.quirkshop.nuisancemaps.repository;

import com.quirkshop.nuisancemaps.model.DataURLCache;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataURLCacheRepository extends CrudRepository<DataURLCache, Integer> {

    @Modifying
    @Query("UPDATE DataURLCache d SET d.count = d.count + 1 WHERE d.url = :url")
    int incrementCountByUrl(@Param("url") String url);
}
