package com.quirkshop.nuisancemaps.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataCrimeController {

    @Autowired
    DataCrimeRepository dataCrimeRepository;

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/datacrimes")
    public List<DataCrime> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

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
    public FeatureCollectionDTO getIndexGeoJSON(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "lat", required = false) String lat,
            @RequestParam(name = "lng", required = false) String lng,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        // defaults
        int distance = 1;
        double latitude = 30.2944;
        double longitude = -97.7171;
        LocalDateTime startDateTime = LocalDateTime.now().minusYears(1);
        LocalDateTime endDateTime = LocalDateTime.now();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
            startDateTime = LocalDateTime.parse(startDate + " 0:00", formatter);
            endDateTime = LocalDateTime.parse(endDate + " 0:00", formatter);
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);

        } catch (Exception e) {
            System.err.println("[Err] parse args " + e.getMessage());
        }

        return dataCrimeRepository.findAllByOrderByReportedAtDescGeoJSON(distance, latitude, longitude,
                startDateTime, endDateTime);
    }

}
