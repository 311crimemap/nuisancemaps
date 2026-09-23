package com.quirkshop.nuisancemaps.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Data311;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Data311Repository extends DataEntityRepository<Data311>, CrudRepository<Data311, Integer> {

    Data311 findOneByReportNum(String reportNum);

    List<Data311> findAllByReportNumIn(List<String> reportNums);

    List<Data311> findAllBySource_Locale_IdAndReportNumIn(Integer localeId, List<String> reportNums);

    List<Data311> findAllBySourceIdAndReportNumIn(Integer sourceId, List<String> reportNums);

    List<Data311> findAllByOrderByReportedAtDesc(PageRequest n);

    @Query(value = "SELECT dc.id AS dc_id, dc.report_num, dc.report_category, dc.address, dc.location, dc.latitude, dc.longitude, dc.reported_at, "
            +
            "cat.id AS cat_id, cat.data_type, cat.text, cat.label, cat.icon_name, cat.icon_unicode " +
            "FROM data_311 dc " +
            "JOIN category cat ON dc.category_id = cat.id " +
            "WHERE ST_Within(point, ST_MakeEnvelope(:sw_lng, :sw_lat, :ne_lng, :ne_lat, 4326)\\:\\:geometry) " +
            "AND dc.reported_at >= :startDate AND dc.reported_at < :endDate " +
            "ORDER BY dc.reported_at DESC, dc.id DESC " +
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
