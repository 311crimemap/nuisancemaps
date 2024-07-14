package com.quirkshop.nuisancemaps.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.repository.Data311Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Data311Controller {

    @Autowired
    Data311Repository data311Repository;

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/data311s")
    public List<Data311> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        if (page != null && limit != null) {
            return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(0, limit));
        }

        return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(0, LIMIT));
    }

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/data311s.geojson")
    public FeatureCollectionDTO getIndexGeoJSON(
            @RequestParam(name = "startDate", required = false) Optional<String> startDate,
            @RequestParam(name = "endDate", required = false) Optional<String> endDate,
            @RequestParam(name = "lat", required = false, defaultValue = "30.2944") Optional<String> lat,
            @RequestParam(name = "lng", required = false, defaultValue = "-97.7171") Optional<String> lng,
            @RequestParam(name = "sw_lat", required = false) Optional<String> sw_lat,
            @RequestParam(name = "sw_lng", required = false) Optional<String> sw_lng,
            @RequestParam(name = "ne_lat", required = false) Optional<String> ne_lat,
            @RequestParam(name = "ne_lng", required = false) Optional<String> ne_lng,
            @RequestParam(name = "limit", required = false) Optional<Integer> limit) {

        final int MAX_LIMIT = 1000;

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

        int _limit = limit.map(Integer::valueOf).orElse(MAX_LIMIT);

        return data311Repository.findAllByBoundsOrderByReportedAtDescGeoJSON(_sw_lat, _sw_lng, _ne_lat, _ne_lng,
                startDateTime, endDateTime, Math.min(_limit, MAX_LIMIT));

    }

}
