package com.quirkshop.nuisancemaps.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.quirkshop.nuisancemaps.dto.CategoryDTO;
import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataCrimeService {
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private DataCrimeRepository dataCrimeRepository;

    /**
     * Retrieves a collection of FeatureDTO based on geographical bounds and
     * date range. Ordered by reportedAt descending.
     *
     * @param sw_lat    The southwest latitude boundary.
     * @param sw_lng    The southwest longitude boundary.
     * @param ne_lat    The northeast latitude boundary.
     * @param ne_lng    The northeast longitude boundary.
     * @param startDate The start date for filtering reports.
     * @param endDate   The end date for filtering reports.
     * @param limit     The maximum number of results to return.
     *
     * @return A FeatureCollectionDTO: meta and list of featureDTOs.
     */

    public FeatureCollectionDTO findAllByBoundsOrderByReportedAtDescGeoJSON(double sw_lat, double sw_lng,
            double ne_lat,
            double ne_lng, LocalDateTime startDate, LocalDateTime endDate, int limit) {

        List<Object[]> results = dataCrimeRepository.findAllByLatLngBoundsAndBetweenDates(sw_lat, sw_lng, ne_lat,
                ne_lng, startDate,
                endDate, limit);

        List<FeatureDTO> featuresDTO = results
                .stream()
                .map(result -> {

                    // Mapping data_crime fields
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
}
