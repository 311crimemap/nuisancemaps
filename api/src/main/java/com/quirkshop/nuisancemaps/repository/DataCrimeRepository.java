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

import com.quirkshop.nuisancemaps.dto.CategoryDTO;
import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataCrime;

@Repository
public interface DataCrimeRepository extends IDataEntityRepository<DataCrime>, CrudRepository<DataCrime, Integer> {

    Page<DataCrime> findAll(Pageable pageRequest); // NB: pagination can issue an additional COUNT query - slow

    List<DataCrime> findAllByOrderByReportedAtDesc(Pageable n);

    DataCrime findOneByReportNum(String reportNum);

    List<DataCrime> findAllByReportNumIn(List<String> reportNums);

    List<DataCrime> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

    default FeatureCollectionDTO findAllByOrderByReportedAtDescGeoJSON(int distance, double latitude, double longitude,
            LocalDateTime startDate, LocalDateTime endDate) {

        List<DataCrime> dataCrimes = findAllByLatLngDistanceAndBetweenDates(distance, latitude, longitude, startDate,
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

    // https://stackoverflow.com/questions/58342156/spring-jpa-query-is-not-recognizing-spatial-types
    // need to escape '::' double instances otherwise query parser thinks it's
    // inserting a variable (single ':')
    //
    // NB: distance * 1609.34 calculation can overflow ~ max 5700 miles
    //
    // CrudRepository knows to appropriately serialize renamed cat.id -> cat_id
    // field
    // but it doesn't know to serialization the nested Category parent
    // which is why its wrapped with a CategoryDTO model
    //
    // cat.id needs to be renamed in the sql to prevent initial conflict
    // e.g. can't "SELECT dc.*, cat.*" with both having "id" columns.

    @Query(value = "SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id FROM data_crime dc "
            +
            "JOIN category cat ON dc.category_id = cat.id WHERE " +
            "ST_Within(point, ST_Buffer(ST_MakePoint(:longitude, :latitude)\\:\\:geography, :distance * 1609.34)\\:\\:geometry) "
            +
            "AND reported_at BETWEEN :startDate AND :endDate ;", nativeQuery = true)
    List<DataCrime> findAllByLatLngDistanceAndBetweenDates(
            @Param("distance") int distance,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    default FeatureCollectionDTO findAllByBoundsOrderByReportedAtDescGeoJSON(double sw_lat, double sw_lng,
            double ne_lat,
            double ne_lng, LocalDateTime startDate, LocalDateTime endDate, int limit) {

        List<DataCrime> dataCrimes = findAllByLatLngBoundsAndBetweenDates(sw_lat, sw_lng, ne_lat, ne_lng, startDate,
                endDate, limit);

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

    @Query(value = "SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id FROM data_crime dc "
            + "JOIN category cat ON dc.category_id = cat.id WHERE " +
            "ST_Within( point, ST_MakeEnvelope(:sw_lng, :sw_lat, :ne_lng, :ne_lat, 4326 )\\:\\:geometry)"
            + "AND reported_at BETWEEN :startDate AND :endDate ORDER BY dc.reported_at DESC LIMIT :limit ;", nativeQuery = true)
    List<DataCrime> findAllByLatLngBoundsAndBetweenDates(
            @Param("sw_lat") double sw_lat,
            @Param("sw_lng") double sw_lng,
            @Param("ne_lat") double ne_lat,
            @Param("ne_lng") double ne_lng,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit);
}
