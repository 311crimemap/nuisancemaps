package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * @return ResponseEntity containing a list of DataError objects, ordered by
     *         their last updated timestamp in descending order.
     *
     */
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/dataerrors")
    public ResponseEntity<?> getIndex(
            @RequestParam(name = "page", required = false) Integer pageParam,
            @RequestParam(name = "limit", required = false) Integer limitParam) {

        final int LIMIT = 50;

        int page = pageParam != null ? pageParam : 0;
        int limit = limitParam != null ? limitParam : LIMIT;

        List<DataError> dataErrors = dataErrorRepository
                .findAllByOrderByUpdatedAtDesc(PageRequest.of(page, limit));

        JSendDTO<List<DataError>> jSendDTO = new JSendDTO<List<DataError>>("success", dataErrors);

        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }
}
