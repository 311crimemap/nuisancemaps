package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.DataCrime;

@Repository
public interface DataCrimeRepository extends CrudRepository<DataCrime, Integer> {
    // auto implemented
    DataCrime findOneByReportNum(String reportNum);

    List<DataCrime> findAllByReportNumIn(List<String> reportNums);
}
