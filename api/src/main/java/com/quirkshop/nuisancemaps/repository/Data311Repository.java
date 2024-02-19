package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
import com.quirkshop.nuisancemaps.model.Data311;

@Repository
public interface Data311Repository extends IDataEntityRepository<Data311>, CrudRepository<Data311, Integer> {

    Data311 findOneByReportNum(String reportNum);

    List<Data311> findAllByReportNumIn(List<String> reportNums);

    List<Data311> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

    List<Data311> findAllByOrderByReportedAtDesc(PageRequest n);

    default FeatureCollectionDTO findAllByOrderByReportedAtDescGeoJSON(PageRequest pageRequest) {

        List<Data311> data311s = findAllByOrderByReportedAtDesc(pageRequest);

        List<FeatureDTO> featuresDTO = data311s
                .stream()
                .map(data311 -> {

                    GeometryDTO g = new GeometryDTO("Point",
                            new Double[] { data311.getLongitude(), data311.getLatitude(), 0.0 });
                    PropertiesDTO p = new PropertiesDTO(data311.getCategory(),
                            data311.getLocation(),
                            data311.getReportedAt(), data311.getReportNum());
                    FeatureDTO f = new FeatureDTO("Feature", g, p);
                    return f;

                }).toList();

        return new FeatureCollectionDTO("FeatureCollection", featuresDTO);

    }

}
