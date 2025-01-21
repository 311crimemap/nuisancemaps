package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataErrorController {

    @Autowired
    DataErrorRepository dataErrorRepository;

    /**
     * Handles HTTP GET requests to fetch a list of DataError entries.
     *
     * @param page  the page number to retrieve, or null to retrieve the first page.
     * @param limit the maximum number of DataError entries to return, or null to
     *              use a default limit.
     * @return a list of DataError objects, ordered by their last updated timestamp
     *         in descending order.
     *
     */
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/dataerrors")
    public List<DataError> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        if (page != null && limit != null) {
            return dataErrorRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            return dataErrorRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            return dataErrorRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, limit));
        }

        return dataErrorRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, LIMIT));
    }
}
