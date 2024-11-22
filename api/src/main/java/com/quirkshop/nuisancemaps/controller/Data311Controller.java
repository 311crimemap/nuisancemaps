package com.quirkshop.nuisancemaps.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.service.Data311Service;
import com.quirkshop.nuisancemaps.util.DataParamValidator;
import com.quirkshop.nuisancemaps.model.DataURLCache;
import com.quirkshop.nuisancemaps.service.DataURLCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Data311Controller {

    @Autowired
    Data311Repository data311Repository;

    @Autowired
    Data311Service data311Service;

    @Autowired
    DataURLCacheService dataURLCacheService;

    private static final int MAX_LIMIT = Integer.parseInt(System.getenv("VITE_MAX_DATA_RECORDS"));

    private int count = 0;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/data311s.geojson")
    @Cacheable(value = "data311ControllerCache", key = "#startDate + '-' + #endDate + '-' + #sw_lat + '-' + #sw_lng + '-' + #ne_lat + '-' + #ne_lng")
    public ResponseEntity<?> getIndexGeoJSON(
            @RequestParam(name = "startDate", required = false) Optional<String> startDate,
            @RequestParam(name = "endDate", required = false) Optional<String> endDate,
            @RequestParam(name = "sw_lat", required = true) String sw_lat,
            @RequestParam(name = "sw_lng", required = true) String sw_lng,
            @RequestParam(name = "ne_lat", required = true) String ne_lat,
            @RequestParam(name = "ne_lng", required = true) String ne_lng) {

        try {

            double _sw_lat = Double.parseDouble(sw_lat);
            double _sw_lng = Double.parseDouble(sw_lng);
            double _ne_lat = Double.parseDouble(ne_lat);
            double _ne_lng = Double.parseDouble(ne_lng);

            DataParamValidator.validateBoundingBox(_sw_lat, _sw_lng, _ne_lat, _ne_lng, 1000);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");

            LocalDateTime startDateTime = startDate
                    .map(date -> LocalDateTime.parse(startDate.get() + " 0:00", formatter))
                    .orElse(LocalDateTime.now().minusMonths(3));

            LocalDateTime endDateTime = endDate
                    .map(date -> LocalDateTime.parse(endDate.get() + " 0:00", formatter))
                    .orElse(LocalDateTime.now());

            count++;
            String logStr = String.format("Data311 %d: %s %s %s %s: ", count, _sw_lat, _sw_lng, _ne_lat, _ne_lng);
            log.info(logStr);

            // NB: cached requests won't reach here, so only fetched queries will be recorded here.
            DataURLCache dataURLCache = new DataURLCache("/data311s.json", startDateTime, endDateTime,
                                                         _sw_lat, _sw_lng, _ne_lat, _ne_lng);
            dataURLCacheService.increment(dataURLCache);


            FeatureCollectionDTO results = data311Service
                    .findAllByBoundsOrderByReportedAtDescGeoJSON(_sw_lat, _sw_lng, _ne_lat, _ne_lng,
                            startDateTime, endDateTime, MAX_LIMIT);

            return ResponseEntity.ok().body(results);

        } catch (Exception e) {
            log.error("[Data311Controller ERR]: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

    }

}
