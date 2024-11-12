package com.quirkshop.nuisancemaps.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataCrimeController {

    @Autowired
    DataCrimeRepository dataCrimeRepository;

    private static final int MAX_LIMIT = Integer.parseInt(System.getenv("VITE_MAX_DATA_RECORDS"));

    private int count = 0;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/datacrimes")
    public List<DataCrime> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 1000;

        if (page != null && limit != null) {
            return dataCrimeRepository.findAllByOrderByReportedAtDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            return dataCrimeRepository.findAllByOrderByReportedAtDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            return dataCrimeRepository.findAllByOrderByReportedAtDesc(PageRequest.of(0, limit));
        }

        return dataCrimeRepository.findAllByOrderByReportedAtDesc(PageRequest.of(0, LIMIT));
    }

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/datacrimes.geojson")
    @Cacheable(value = "dataCrimeControllerCache", key = "#startDate + '-' + #endDate + '-' + #sw_lat + '-' + #sw_lng + '-' + #ne_lat + '-' + #ne_lng")
    public FeatureCollectionDTO getIndexGeoJSON(
            @RequestParam(name = "startDate", required = false) Optional<String> startDate,
            @RequestParam(name = "endDate", required = false) Optional<String> endDate,
            @RequestParam(name = "lat", required = false, defaultValue = "30.2944") Optional<String> lat,
            @RequestParam(name = "lng", required = false, defaultValue = "-97.7171") Optional<String> lng,
            @RequestParam(name = "sw_lat", required = false) Optional<String> sw_lat,
            @RequestParam(name = "sw_lng", required = false) Optional<String> sw_lng,
            @RequestParam(name = "ne_lat", required = false) Optional<String> ne_lat,
            @RequestParam(name = "ne_lng", required = false) Optional<String> ne_lng) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");

        LocalDateTime startDateTime = startDate
                .map(date -> LocalDateTime.parse(startDate.get() + " 0:00", formatter))
                .orElse(LocalDateTime.now().minusYears(1));

        LocalDateTime endDateTime = endDate
                .map(date -> LocalDateTime.parse(endDate.get() + " 0:00", formatter))
                .orElse(LocalDateTime.now());

        double _lat = lat.map(Double::parseDouble).get();
        double _lng = lng.map(Double::parseDouble).get();

        double _sw_lat = sw_lat.map(Double::parseDouble).orElse(_lat);
        double _sw_lng = sw_lng.map(Double::parseDouble).orElse(_lng);
        double _ne_lat = ne_lat.map(Double::parseDouble).orElse(_lat);
        double _ne_lng = ne_lng.map(Double::parseDouble).orElse(_lng);

        count++;
        String logStr = String.format("DataCrime %d: %s %s %s %s: ", count, _sw_lat, _sw_lng, _ne_lat, _ne_lng);
        log.info(logStr);

        return dataCrimeRepository.findAllByBoundsOrderByReportedAtDescGeoJSON(_sw_lat, _sw_lng, _ne_lat, _ne_lng,
                startDateTime, endDateTime, MAX_LIMIT);
    }

}
