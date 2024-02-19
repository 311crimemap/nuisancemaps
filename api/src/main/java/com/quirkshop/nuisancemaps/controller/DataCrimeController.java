package com.quirkshop.nuisancemaps.controller;

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
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        if (page != null && limit != null) {
            return dataCrimeRepository.findAllByOrderByReportedAtDescGeoJSON(PageRequest.of(page,
                    limit));
        } else if (page != null) {
            return dataCrimeRepository.findAllByOrderByReportedAtDescGeoJSON(PageRequest.of(page,
                    LIMIT));
        } else if (limit != null) {
            return dataCrimeRepository.findAllByOrderByReportedAtDescGeoJSON(PageRequest.of(0,
                    limit));
        }

        return dataCrimeRepository.findAllByOrderByReportedAtDescGeoJSON(PageRequest.of(0, LIMIT));
    }

}
