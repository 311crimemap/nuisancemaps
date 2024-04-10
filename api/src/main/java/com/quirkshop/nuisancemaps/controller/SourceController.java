package com.quirkshop.nuisancemaps.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
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
            jSendDTO = new JSendDTO("success", source);
        } catch(DataIntegrityViolationException e) {
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
        List<Source> res = new ArrayList<Source>();

        for (Source source : sources) {
            try {
                source = sourceLoaderService.saveTransaction(source);
                res.add(source);
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
        return ResponseEntity.status(HttpStatus.OK).body(sourceIter);
    }

    @GetMapping("/sources/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        Source source = sourceRepository.findById(id).orElse(null);
        if (source != null)
            return ResponseEntity.status(HttpStatus.OK).body(source);

        return ResponseEntity.status(404).body(null);
    }

    @PatchMapping("/sources/{id}")
    public ResponseEntity<?> patch(@PathVariable(value = "id") final int id,
            @RequestBody Source jsonSource) {

        Optional<Source> optionalSource = sourceRepository.findById(id);
        if (!optionalSource.isPresent())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");

        final Source finalSource = optionalSource.get();

        ReflectionUtils.doWithFields(Source.class, field -> {
            field.setAccessible(true);
            Object value = field.get(jsonSource);
            if (value != null) {
                field.set(finalSource, value);
            }
        });

        Source res = sourceRepository.save(finalSource);
        return ResponseEntity.status(HttpStatus.OK).body(res);

    }

}
