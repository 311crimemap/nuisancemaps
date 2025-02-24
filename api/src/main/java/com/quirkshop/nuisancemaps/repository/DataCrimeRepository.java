package com.quirkshop.nuisancemaps.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.quirkshop.nuisancemaps.model.DataCrime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataCrimeRepository extends DataEntityRepository<DataCrime>, CrudRepository<DataCrime, Integer> {

    Page<DataCrime> findAll(Pageable pageRequest); // NB: pagination can issue an additional COUNT query - slow

    List<DataCrime> findAllByOrderByReportedAtDesc(Pageable n);

    DataCrime findOneByReportNum(String reportNum);

    List<DataCrime> findAllByReportNumIn(List<String> reportNums);

    List<DataCrime> findAllBySource_Locale_IdAndReportNumIn(Integer localeId, List<String> reportNums);

    List<DataCrime> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

    /**
     * Reference: Spring JPA query handling of spatial types:
     * https://stackoverflow.com/questions/58342156/spring-jpa-query-is-not-recognizing-spatial-types.
     *
     * Need to escape '::' double instances in the query; otherwise, the query
     * parser may interpret it incorrectly as a variable (single ':').
     *
     * The distance * 1609.34 calculation may overflow at approximately 5700 miles.
     *
     * The CrudRepository correctly serializes the renamed `cat.id` to `cat_id`.
     * However, it does not know to serialize the nested Category parent object,
     * which is why it is wrapped in a CategoryDTO model.
     *
     * Renaming `cat.id` in the SQL query is needed to avoid naming conflicts,
     * e.g., you cannot have "SELECT dc.*, cat.*" when both columns have "id".
     */
    @Query(value = "SELECT dc.id AS dc_id, dc.report_num, dc.report_category, dc.address, dc.location, dc.latitude, dc.longitude, dc.reported_at, "
            +
            "cat.id AS cat_id, cat.data_type, cat.text, cat.label, cat.icon_name, cat.icon_unicode " +
            "FROM data_crime dc " +
            "JOIN category cat ON dc.category_id = cat.id " +
            "WHERE ST_Within(point, ST_MakeEnvelope(:sw_lng, :sw_lat, :ne_lng, :ne_lat, 4326)\\:\\:geometry) " +
            "AND reported_at BETWEEN :startDate AND :endDate " +
            "ORDER BY dc.reported_at DESC " +
            "LIMIT :limit", nativeQuery = true)

    List<Object[]> findAllByLatLngBoundsAndBetweenDates(
            @Param("sw_lat") double sw_lat,
            @Param("sw_lng") double sw_lng,
            @Param("ne_lat") double ne_lat,
            @Param("ne_lng") double ne_lng,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit);
}
