package com.quirkshop.nuisancemaps.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.quirkshop.nuisancemaps.dto.CategoryDTO;
import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
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

    default FeatureCollectionDTO findAllByBoundsOrderByReportedAtDescGeoJSON(double sw_lat, double sw_lng,
            double ne_lat, double ne_lng, LocalDateTime startDate, LocalDateTime endDate, int limit) {

        List<Object[]> results = findAllByLatLngBoundsAndBetweenDates(sw_lat, sw_lng, ne_lat, ne_lng, startDate,
                endDate, limit);

        List<FeatureDTO> featuresDTO = results
                .stream()
                .map(result -> {

                    // Mapping data_311 fields
                    Long dcId = (Long) result[0];
                    String reportNum = (String) result[1];
                    String reportCategory = (String) result[2];
                    String address = (String) result[3];
                    String location = (String) result[4];
                    Double latitude = (Double) result[5];
                    Double longitude = (Double) result[6];
                    Timestamp t_reportedAt = ((Timestamp) result[7]);
                    LocalDateTime reportedAt = t_reportedAt.toLocalDateTime();

                    // Mapping category fields
                    Integer catId = (Integer) result[8];
                    String dataType = (String) result[9];
                    String text = (String) result[10];
                    Integer label = (Integer) result[11];
                    String iconName = (String) result[12];
                    String iconUnicode = (String) result[13];

                    GeometryDTO g = new GeometryDTO("Point",
                            new Double[] { longitude, latitude, 0.0 });

                    CategoryDTO cDTO = new CategoryDTO(catId, dataType, text, label, iconName, iconUnicode);

                    PropertiesDTO p = new PropertiesDTO(reportCategory,
                            address,
                            location,
                            reportedAt,
                            reportNum,
                            cDTO);

                    FeatureDTO f = new FeatureDTO("Feature", g, p);
                    return f;

                }).toList();

        return new FeatureCollectionDTO("FeatureCollection", featuresDTO);

    }

    @Query(value = "SELECT dc.id AS dc_id, dc.report_num, dc.report_category, dc.address, dc.location, dc.latitude, dc.longitude, dc.reported_at, "
            +
            "cat.id AS cat_id, cat.data_type, cat.text, cat.label, cat.icon_name, cat.icon_unicode " +
            "FROM data_311 dc " +
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
