package com.quirkshop.nuisancemaps.repository;

import java.util.List;
import com.quirkshop.nuisancemaps.model.IDataEntity;

import org.springframework.data.repository.CrudRepository;

public interface IDataEntityRepository<IDataEntity> {
    // wrap default CrudRepository method for saveAll
    default Iterable<IDataEntity> saveAllEntities(Iterable<IDataEntity> entities) {
        return ((CrudRepository<IDataEntity, Integer>) this).saveAll(entities);
    }

    // Abstract query method for findAllBySourceIdAndReportNumIn
    List<IDataEntity> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

}
