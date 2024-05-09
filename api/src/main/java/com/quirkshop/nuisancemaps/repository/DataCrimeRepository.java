package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                    PropertiesDTO p = new PropertiesDTO(dataCrime.getReportCategory(),
                            dataCrime.getLocation(),
                            dataCrime.getReportedAt(), dataCrime.getReportNum(), dataCrime.getOrgCategory());
                    FeatureDTO f = new FeatureDTO("Feature", g, p);
                    return f;

                }).toList();

        return new FeatureCollectionDTO("FeatureCollection", featuresDTO);

    }

    // https://stackoverflow.com/questions/58342156/spring-jpa-query-is-not-recognizing-spatial-types
    // need to escape '::' double instances otherwise query parser thinks it's
    // inserting a variable (single ':')
    //
    //NB: distance * 1609.34 calculation can overflow ~ max 5700 miles
    //
    @Query(value = "SELECT * FROM data_crime WHERE " +
            "ST_Within(point, ST_Buffer(ST_MakePoint(:longitude, :latitude)\\:\\:geography, :distance * 1609.34)\\:\\:geometry) " +
            "AND reported_at BETWEEN :startDate AND :endDate ;", nativeQuery = true)
    List<DataCrime> findCrimesWithinDistance(
            @Param("distance") int distance,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}
