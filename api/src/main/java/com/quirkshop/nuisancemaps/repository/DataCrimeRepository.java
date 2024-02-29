package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
import com.quirkshop.nuisancemaps.model.DataCrime;

@Repository
public interface DataCrimeRepository extends IDataEntityRepository<DataCrime>, CrudRepository<DataCrime, Integer> {

    Page<DataCrime> findAll(Pageable pageRequest); // NB: pagination can issue an additional COUNT query - slow

    List<DataCrime> findAllByOrderByReportedAtDesc(Pageable n);

    DataCrime findOneByReportNum(String reportNum);

    List<DataCrime> findAllByReportNumIn(List<String> reportNums);

    List<DataCrime> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

    default FeatureCollectionDTO findAllByOrderByReportedAtDescGeoJSON(PageRequest pageRequest) {

        List<DataCrime> dataCrimes = findAllByOrderByReportedAtDesc(pageRequest);

        List<FeatureDTO> featuresDTO = dataCrimes
            .stream()
            .map(dataCrime -> {

                    GeometryDTO g = new GeometryDTO("Point",
                                                    new Double[] { dataCrime.getLongitude(), dataCrime.getLatitude(), 0.0 });
                    PropertiesDTO p = new PropertiesDTO(dataCrime.getCategory(),
                                                        dataCrime.getLocation(),
                                                        dataCrime.getReportedAt(), dataCrime.getReportNum());
                    FeatureDTO f = new FeatureDTO("Feature", g, p);
                    return f;

                }).toList();


        return new FeatureCollectionDTO("FeatureCollection", featuresDTO);

    }

}
