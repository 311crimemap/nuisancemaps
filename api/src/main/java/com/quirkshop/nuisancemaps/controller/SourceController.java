package com.quirkshop.nuisancemaps.controller;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.SourceLoaderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.ReflectionUtils;

import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.SourceDTO;

@RestController
public class SourceController {

    @Autowired
    MappingRepository mappingRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    SourceLoaderService sourceLoaderService;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @PostMapping("/sources")
    public ResponseEntity<?> create(@RequestBody Source source) {
        JSendDTO jSendDTO;

        try {
            source = sourceLoaderService.saveTransaction(source);

            Double[] location = { source.getLocation().getX(), source.getLocation().getY() };

            SourceDTO res = new SourceDTO(source.getSourceConfigId(),
                    source.getSourceConfigEntity(),
                    source.getSourceConfigNotes(),
                    location,
                    source.getIconName(),
                    source.getIconUnicode(),
                    source.getCategory(),
                    source.getDescription(),
                    source.getNumRecords());

            jSendDTO = new JSendDTO("success", res);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

    @PostMapping("/sources/batch")
    @Transactional
    public ResponseEntity<?> createBatch(@RequestBody List<Source> sources) {
        JSendDTO jSendDTO;
        List<SourceDTO> res = new ArrayList<SourceDTO>();

        for (Source source : sources) {
            try {
                source = sourceLoaderService.saveTransaction(source);

                Double[] location = { source.getLocation().getX(), source.getLocation().getY() };

                SourceDTO sourceDTO = new SourceDTO(source.getSourceConfigId(),
                        source.getSourceConfigEntity(),
                        source.getSourceConfigNotes(),
                        location,
                        source.getIconName(),
                        source.getIconUnicode(),
                        source.getCategory(),
                        source.getDescription(),
                        source.getNumRecords());

                res.add(sourceDTO);
            } catch (DataIntegrityViolationException e) {
                log.error(e.getMessage());
            }

        }

        if (res.size() > 0) {
            jSendDTO = new JSendDTO("success", res);
        } else {
            jSendDTO = new JSendDTO("nothing saved", res);
        }

        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/sources")
    public ResponseEntity<?> index() {
        Iterable<Source> sourceIter = sourceRepository.findAll();
        ArrayList<SourceDTO> res = new ArrayList<SourceDTO>();

        for (Source source : sourceIter) {

            Double[] location = { source.getLocation().getX(), source.getLocation().getY() };

            SourceDTO sourceDTO = new SourceDTO(source.getSourceConfigId(),
                    source.getSourceConfigEntity(),
                    source.getSourceConfigNotes(),
                    location,
                    source.getIconName(),
                    source.getIconUnicode(),
                    source.getCategory(),
                    source.getDescription(),
                    source.getNumRecords());

            res.add(sourceDTO);
        }

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/sources/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        Source source = sourceRepository.findById(id).orElse(null);
        if (source != null) {

            Double[] location = { source.getLocation().getX(), source.getLocation().getY() };

            SourceDTO sourceDTO = new SourceDTO(source.getSourceConfigId(),
                    source.getSourceConfigEntity(),
                    source.getSourceConfigNotes(),
                    location,
                    source.getIconName(),
                    source.getIconUnicode(),
                    source.getCategory(),
                    source.getDescription(),
                    source.getNumRecords());

            return ResponseEntity.status(HttpStatus.OK).body(sourceDTO);
        }

        return ResponseEntity.status(404).body(null);
    }

    @GetMapping("/sources/{id}/updateNumRecords")
    public ResponseEntity<?> updateNumRecords(@PathVariable(value = "id") final int id) {
        JSendDTO jSendDTO;
        Source source = sourceRepository.findById(id).orElse(null);

        if (source != null) {
            log.info("Source ID: " + source.getId() + " Fetch updateNumRecords");

            int numRecords = sourceLoaderService.fetchCount(source);
            log.info("prev: " + source.getNumRecords() + " new: " + numRecords);

            source.setNumRecords(numRecords);
            source = sourceLoaderService.saveTransaction(source);

            Double[] location = { source.getLocation().getX(), source.getLocation().getY() };

            SourceDTO res = new SourceDTO(source.getSourceConfigId(),
                    source.getSourceConfigEntity(),
                    source.getSourceConfigNotes(),
                    location,
                    source.getIconName(),
                    source.getIconUnicode(),
                    source.getCategory(),
                    source.getDescription(),
                    source.getNumRecords());

            jSendDTO = new JSendDTO("success", res);

            return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
        }

        return ResponseEntity.status(404).body(null);
    }

    @PatchMapping("/sources/{id}")
    public ResponseEntity<?> patch(@PathVariable(value = "id") final int id,
            @RequestBody Map<String, Object> updates) {
        JSendDTO jSendDTO;
        Source updatedSource = null;
        try {
            updatedSource = sourceLoaderService.updateSource(id, updates);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        Double[] location = { updatedSource.getLocation().getX(), updatedSource.getLocation().getY() };

        SourceDTO res = new SourceDTO(updatedSource.getSourceConfigId(),
                updatedSource.getSourceConfigEntity(),
                updatedSource.getSourceConfigNotes(),
                location,
                updatedSource.getIconName(),
                updatedSource.getIconUnicode(),
                updatedSource.getCategory(),
                updatedSource.getDescription(),
                updatedSource.getNumRecords());

        jSendDTO = new JSendDTO("success", res);
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);

    }

}
