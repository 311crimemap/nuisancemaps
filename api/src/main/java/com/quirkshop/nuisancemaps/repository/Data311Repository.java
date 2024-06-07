package com.quirkshop.nuisancemaps.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.dto.CategoryDTO;
import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.Category;

@Repository
public interface Data311Repository extends IDataEntityRepository<Data311>, CrudRepository<Data311, Integer> {

    Data311 findOneByReportNum(String reportNum);

    List<Data311> findAllByReportNumIn(List<String> reportNums);

    List<Data311> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

    List<Data311> findAllByOrderByReportedAtDesc(PageRequest n);

    default FeatureCollectionDTO findAllByOrderByReportedAtDescGeoJSON(int distance, double latitude, double longitude,
            LocalDateTime startDate, LocalDateTime endDate) {

        List<Data311> data311s = findAllByLatLngDistanceAndBetweenDates(distance, latitude, longitude, startDate,
                endDate);

        List<FeatureDTO> featuresDTO = data311s
                .stream()
                .map(data311 -> {

                    GeometryDTO g = new GeometryDTO("Point",
                            new Double[] { data311.getLongitude(), data311.getLatitude(), 0.0 });

                    Category c = data311.getOrgCategory();

                    CategoryDTO cDTO = new CategoryDTO(c.getId(), c.getDataType(), c.getText(), c.getLabel(),
                            c.getIconName(), c.getIconUnicode());

                    PropertiesDTO p = new PropertiesDTO(data311.getReportCategory(),
                            data311.getLocation(),
                            data311.getReportedAt(), data311.getReportNum(), cDTO);

                    FeatureDTO f = new FeatureDTO("Feature", g, p);
                    return f;

                }).toList();

        return new FeatureCollectionDTO("FeatureCollection", featuresDTO);

    }

    @Query(value = "SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id FROM data_311 dc "
            +
            "JOIN category cat ON dc.category_id = cat.id WHERE " +
            "ST_Within(point, ST_Buffer(ST_MakePoint(:longitude, :latitude)\\:\\:geography, :distance * 1609.34)\\:\\:geometry) "
            +
            "AND reported_at BETWEEN :startDate AND :endDate ;", nativeQuery = true)
    List<Data311> findAllByLatLngDistanceAndBetweenDates(
            @Param("distance") int distance,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    default FeatureCollectionDTO findAllByBoundsOrderByReportedAtDescGeoJSON(double sw_lat, double sw_lng,
            double ne_lat,
            double ne_lng, LocalDateTime startDate, LocalDateTime endDate) {

        List<Data311> dataCrimes = findAllByLatLngBoundsAndBetweenDates(sw_lat, sw_lng, ne_lat, ne_lng, startDate,
                endDate);

        List<FeatureDTO> featuresDTO = dataCrimes
                .stream()
                .map(dataCrime -> {

                    GeometryDTO g = new GeometryDTO("Point",
                            new Double[] { dataCrime.getLongitude(), dataCrime.getLatitude(), 0.0 });

                    Category c = dataCrime.getOrgCategory();

                    CategoryDTO cDTO = new CategoryDTO(c.getId(), c.getDataType(), c.getText(), c.getLabel(),
                            c.getIconName(), c.getIconUnicode());

                    PropertiesDTO p = new PropertiesDTO(dataCrime.getReportCategory(),
                            dataCrime.getLocation(),
                            dataCrime.getReportedAt(), dataCrime.getReportNum(), cDTO);

                    FeatureDTO f = new FeatureDTO("Feature", g, p);
                    return f;

                }).toList();

        return new FeatureCollectionDTO("FeatureCollection", featuresDTO);

    }


    @Query(value = "SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id FROM data_311 dc "
            + "JOIN category cat ON dc.category_id = cat.id WHERE " +
            "ST_Within( point, ST_MakeEnvelope(:sw_lng, :sw_lat, :ne_lng, :ne_lat, 4326 )\\:\\:geometry)"
            + "AND reported_at BETWEEN :startDate AND :endDate ;", nativeQuery = true)
    List<Data311> findAllByLatLngBoundsAndBetweenDates(
            @Param("sw_lat") double sw_lat,
            @Param("sw_lng") double sw_lng,
            @Param("ne_lat") double ne_lat,
            @Param("ne_lng") double ne_lng,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}
