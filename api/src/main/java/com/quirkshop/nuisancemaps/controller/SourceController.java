package com.quirkshop.nuisancemaps.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.SourceDTO;

@RestController
public class SourceController {

    @Autowired
    LocaleRepository localeRepository;

    @Autowired
    MappingRepository mappingRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    SourceLoaderService sourceLoaderService;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @PostMapping("/locales/{id}/sources")
    public ResponseEntity<?> create(@PathVariable("id") Integer locale_id, @RequestBody Source source) {
        JSendDTO jSendDTO;
        Locale locale = localeRepository.findById(locale_id).orElse(null);
        if (locale == null) {
            jSendDTO = new JSendDTO("not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jSendDTO);
        }

        try {
            source.setLocale(locale);
            source = sourceLoaderService.saveTransaction(source);
            jSendDTO = new JSendDTO("success", source.toDTO());
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

    @PostMapping("/locales/{id}/sources/batch")
    @Transactional
    public ResponseEntity<?> createBatch(@PathVariable("id") Integer locale_id,
                                         @RequestBody List<Source> sources) {
        JSendDTO jSendDTO;
        List<SourceDTO> res = new ArrayList<SourceDTO>();
        Locale locale = localeRepository.findById(locale_id).orElse(null);
        if (locale == null) {
            jSendDTO = new JSendDTO("not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jSendDTO);
        }

        for (Source source : sources) {
            try {
                source.setLocale(locale);
                source = sourceLoaderService.saveTransaction(source);
                res.add(source.toDTO());
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
            res.add(source.toDTO());
        }

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/locales/{id}/sources")
    public ResponseEntity<?> getLocaleSources(@PathVariable(value = "id") final int id) {
        JSendDTO jSendDTO;
        Locale locale = localeRepository.findById(id).orElse(null);
        if (locale == null) {
            jSendDTO = new JSendDTO("not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jSendDTO);
        }

        List<Source> source = sourceRepository.findAllByLocaleId(id);
        List<SourceDTO> sourceDTOs = source.stream().map(Source::toDTO).collect(Collectors.toList());
        jSendDTO = new JSendDTO("success", sourceDTOs);

        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

    @GetMapping("/sources/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        Source source = sourceRepository.findById(id).orElse(null);
        if (source != null) {
            return ResponseEntity.status(HttpStatus.OK).body(source.toDTO());
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
            jSendDTO = new JSendDTO("success", source.toDTO());

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

        jSendDTO = new JSendDTO("success", updatedSource.toDTO());
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);

    }

}
