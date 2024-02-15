package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.quirkshop.nuisancemaps.model.DataCrime;

@Repository
public interface DataCrimeRepository extends IDataEntityRepository<DataCrime>, CrudRepository<DataCrime, Integer> {

    List<DataCrime> findAllByOrderByReportedAtDesc(PageRequest n);

    DataCrime findOneByReportNum(String reportNum);

    List<DataCrime> findAllByReportNumIn(List<String> reportNums);

    List<DataCrime> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);
}
