package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.quirkshop.nuisancemaps.model.Data311;

@Repository
public interface Data311Repository extends IDataEntityRepository<Data311>, CrudRepository<Data311, Integer> {

    Data311 findOneByReportNum(String reportNum);

    List<Data311> findAllByReportNumIn(List<String> reportNums);

    List<Data311> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);
}
