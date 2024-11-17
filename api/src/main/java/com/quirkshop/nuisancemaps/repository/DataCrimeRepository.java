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
