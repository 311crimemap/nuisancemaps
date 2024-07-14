package com.quirkshop.nuisancemaps.controller;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

@RestController
public class DataJobController {

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    SourceRepository sourceRepository;

    private final int PARAM_LIMIT = 10000;

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/datajobs")
    public List<DataJob> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        if (page != null && limit != null) {
            return dataJobRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            return dataJobRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            return dataJobRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, limit));
        }

        return dataJobRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, LIMIT));
    }

    // only want to toggle Status for now
    // curl -H "content-type: application/json" -X PATCH -d '{"status":"QUEUED"}'
    // localhost:8080/datajobs/1124

    @PatchMapping(path = "/datajobs/{id}")
    public ResponseEntity<?> patch(@PathVariable(value = "id") final int id,
            @RequestBody com.fasterxml.jackson.databind.JsonNode payload) {

        Map<String, String> response = new HashMap<String, String>();
        Optional<DataJob> dataJob = dataJobRepository.findById(id);

        if (dataJob.isPresent()) {
            String status = payload.get("status").asText();
            try {
                DataJob d = dataJob.get();
                d.setStatus(DataJobStatus.valueOf(status));
                d = dataJobRepository.save(d);
                return ResponseEntity.ok(d);
            } catch (IllegalArgumentException e) {
                response.put("id", Integer.toString(id));
                response.put("error", "illegal Parameter");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        response.put("id", Integer.toString(id));
        response.put("error", "not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

    }

    // new session per source
    // this is where we initiate a new crawl session
    // curl -H 'X-API-KEY: <API-KEY>' -X POST localhost:8080/datajobs/sources/1
    @PostMapping("/datajobs/sources/{sourceId}")
    public ResponseEntity<?> createNewSession(@PathVariable(value = "sourceId") final int sourceId) {

        Map<String, String> response = new HashMap<String, String>();
        DataJob dataJob;
        Source source = sourceRepository.findById(sourceId).orElse(null);

        if (source == null) {
            response.put("sourceId", Integer.toString(sourceId));
            response.put("error", "source does not exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            dataJob = dataJobRepository.createNewDataJob(source, 0, PARAM_LIMIT, null);
        } catch (UnsupportedEncodingException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok().body(dataJob);
    }

    // restart all non completes
    @GetMapping("/datajobs/restart")
    public ResponseEntity<?> restartNonCompleted() {
        List<DataJobStatus> excludedStatuses = Arrays.asList(DataJobStatus.QUEUED, DataJobStatus.COMPLETED);
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);

        int numUpdated = dataJobRepository
                .updateAllIncompleteToQueuedBefore(DataJobStatus.QUEUED, LocalDateTime.now(), excludedStatuses, dayAgo);

        Map<String, String> response = new HashMap<String, String>();
        response.put("numUpdated", Integer.toString(numUpdated));
        return ResponseEntity.ok().body(response);
    }
}
