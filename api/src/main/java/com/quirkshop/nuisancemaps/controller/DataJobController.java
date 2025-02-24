package com.quirkshop.nuisancemaps.controller;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.DataJobService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataJobController {

    @Autowired
    DataJobService dataJobService;

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    SourceRepository sourceRepository;

    private static final int PARAM_LIMIT = Integer.parseInt(System.getenv("WORKER_QUERY_LIMIT"));

    /**
     * Retrieves a paginated list of data jobs, ordered by their updated
     * timestamp.
     *
     * @param page  the page number to retrieve (optional)
     * @param limit the number of items per page (optional, defaults to 50)
     * @return a ResponseEntity list of DataJob objects
     */
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/datajobs")
    public ResponseEntity<?> getIndex(
            @RequestParam(name = "page", required = false) Integer pageParam,
            @RequestParam(name = "limit", required = false) Integer limitParam) {

        final int LIMIT = 50;

        int page = pageParam != null ? pageParam : 0;
        int limit = limitParam != null ? limitParam : LIMIT;

        List<DataJob> dataJobs = dataJobRepository
                .findAllByOrderByUpdatedAtDesc(PageRequest.of(page, limit));

        JSendDTO<List<DataJob>> jSendDTO = new JSendDTO<List<DataJob>>("success", dataJobs);
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

    /**
     * Updates (only) the status of a specific data job identified by its ID.
     *
     * curl -H "content-type: application/json" -X PATCH -d '{"status":"QUEUED"}'
     * localhost:8080/datajobs/1124
     *
     * @param id      the ID of the data job to update
     * @param payload a JSON payload containing the new status
     * @return ResponseEntity containing the updated DataJob or an error message
     */

    @PatchMapping(path = "/datajobs/{id}")
    public ResponseEntity<?> patch(@PathVariable(value = "id") final int id,
            @RequestBody com.fasterxml.jackson.databind.JsonNode payload) {

        Map<String, String> response = new HashMap<String, String>();
        response.put("id", Integer.toString(id));

        Optional<DataJob> dataJob = dataJobRepository.findById(id);

        if (!dataJob.isPresent()) {
            response.put("msg", "not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSendDTO<Map<String, String>>("error", response));
        }

        String status = payload.get("status").asText();

        try {
            DataJob d = dataJob.get();
            d.setStatus(DataJobStatus.valueOf(status));
            if (d.getParameters() == null) {
                d.setParameters(new HashMap<>()); // Initialize if null
            }
            d = dataJobRepository.save(d);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new JSendDTO<DataJob>("success", d));

        } catch (IllegalArgumentException e) {
            response.put("msg", "illegal Parameter");
        } catch (Exception e) {
            response.put("msg", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JSendDTO<Map<String, String>>("error", response));
    }

    /**
     * Initiates a new crawl session for a given Source.
     *
     * curl -H 'X-API-KEY: <API-KEY>' -X POST localhost:8080/datajobs/sources/1
     *
     * @param sourceId the ID of the source to create a new session for
     * @return ResponseEntity containing the newly created DataJob or an error
     *         message
     */
    @PostMapping("/datajobs/sources/{sourceId}")
    public ResponseEntity<?> createNewSession(@PathVariable(value = "sourceId") final int sourceId) {

        Map<String, String> response = new HashMap<String, String>();
        DataJob dataJob;
        Optional<Source> source = sourceRepository.findById(sourceId);

        if (!source.isPresent()) {
            response.put("sourceId", Integer.toString(sourceId));
            response.put("msg", "source does not exist");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSendDTO<Map<String, String>>("error", response));
        }

        try {
            dataJob = dataJobService.createNewDataJob(source.get(), null);
            if (dataJob == null) {
                response.put("msg", "No next job");
                return ResponseEntity.ok()
                        .body(new JSendDTO<Map<String, String>>("success", response));
            }

            return ResponseEntity.ok()
                    .body(new JSendDTO<DataJob>("success", dataJob));

        } catch (Exception e) {
            response.put("msg", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JSendDTO<Map<String, String>>("error", response));
    }

    /*
     * Error and Restarts
     */

    /**
     * Retrieves a list of all data jobs with non-completed statuses (errors).
     *
     * @return ResponseEntity containing a list of DataJob objects in error
     *         statuses
     */
    @GetMapping("/datajobs/errors")
    public ResponseEntity<?> findAllErrorJobs() {

        List<DataJobStatus> statuses = Arrays.asList(DataJobStatus.FETCH_ERROR,
                DataJobStatus.PARSE_ERROR,
                DataJobStatus.ERROR);

        List<DataJob> dataJobs = dataJobRepository.findAllInStatuses(statuses);
        return ResponseEntity.ok()
                .body(new JSendDTO<List<DataJob>>("success", dataJobs));
    }

    /**
     * Restarts all dangling data jobs that have not been completed within the last
     * day. Only want recently broken jobs (e.g. per that crawl session)
     *
     * @return ResponseEntity containing the number of jobs updated to 'QUEUED'
     *         status
     */
    @GetMapping("/datajobs/restartAll")
    public ResponseEntity<?> restartAllDangling() {

        List<DataJobStatus> excludedStatuses = Arrays.asList(DataJobStatus.COMPLETED,
                DataJobStatus.QUEUED);

        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);

        int numUpdated = dataJobRepository
                .updateAllIncompleteToQueuedBefore(DataJobStatus.QUEUED,
                        LocalDateTime.now(),
                        excludedStatuses,
                        dayAgo);

        Map<String, String> response = new HashMap<String, String>();
        response.put("numUpdated", Integer.toString(numUpdated));
        return ResponseEntity.ok()
                .body(new JSendDTO<Map<String, String>>("success", response));
    }

    /**
     * Restarts all data jobs that are in error states from the last day.
     *
     * @return ResponseEntity containing the number of jobs updated to 'QUEUED'
     *         status
     */
    @GetMapping("/datajobs/restart")
    public ResponseEntity<?> restartErrors() {

        List<DataJobStatus> statuses = Arrays.asList(DataJobStatus.FETCH_ERROR,
                DataJobStatus.PARSE_ERROR,
                DataJobStatus.ERROR);

        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);

        int numUpdated = dataJobRepository
                .updateAllErrorsToQueuedBefore(DataJobStatus.QUEUED,
                        LocalDateTime.now(),
                        statuses,
                        dayAgo);

        Map<String, String> response = new HashMap<String, String>();
        response.put("numUpdated", Integer.toString(numUpdated));
        return ResponseEntity.ok()
                .body(new JSendDTO<Map<String, String>>("success", response));
    }
}
