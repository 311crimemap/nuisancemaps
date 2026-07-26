package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.DataEntity;

import org.springframework.data.repository.CrudRepository;

public interface DataEntityRepository<T extends DataEntity> {

    // wrap default CrudRepository method for saveAll
    default Iterable<DataEntity> saveAllEntities(Iterable<DataEntity> entities) {
        return ((CrudRepository<DataEntity, Integer>) this).saveAll(entities);
    }

    // Matches the data table's (source_id, report_num) unique constraint.
    List<T> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

}
