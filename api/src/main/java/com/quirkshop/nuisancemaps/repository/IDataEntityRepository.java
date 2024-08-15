package com.quirkshop.nuisancemaps.repository;

import java.util.List;
import com.quirkshop.nuisancemaps.model.IDataEntity;

import org.springframework.data.repository.CrudRepository;

public interface IDataEntityRepository<T extends IDataEntity> {
    // wrap default CrudRepository method for saveAll
    default Iterable<IDataEntity> saveAllEntities(Iterable<IDataEntity> entities) {
        return ((CrudRepository<IDataEntity, Integer>) this).saveAll(entities);
    }

    // Abstract query method for findAllBySourceIdAndReportNumIn
    List<T> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

}
