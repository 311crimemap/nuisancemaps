package com.quirkshop.nuisancemaps.repository;

import java.util.List;
import com.quirkshop.nuisancemaps.model.DataEntity;

import org.springframework.data.repository.CrudRepository;

public interface DataEntityRepository<T extends DataEntity> {
    // wrap default CrudRepository method for saveAll
    default Iterable<DataEntity> saveAllEntities(Iterable<DataEntity> entities) {
        return ((CrudRepository<DataEntity, Integer>) this).saveAll(entities);
    }

    List<T> findAllBySource_Locale_IdAndReportNumIn(Integer localeId, List<String> reportNums);

    // Abstract query method for findAllBySourceIdAndReportNumIn
    List<T> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

}
