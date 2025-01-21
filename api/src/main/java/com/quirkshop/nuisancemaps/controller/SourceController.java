package com.quirkshop.nuisancemaps.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.SourceDTO;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.SourceLoaderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Creates a new source for the specified locale.
     *
     * curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' \
     * -d @source.json localhost:8080/locales/{id}/sources
     *
     * @param locale_id the ID of the locale to which the source is associated
     * @param source    the source object to be created
     * @return a response entity containing the Source DTO
     */
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

    /**
     * Creates a batch of sources for the specified locale.
     *
     * @param locale_id the ID of the locale to which the sources are associated
     * @param sources   a list of source objects to be created
     * @return a response entity containing Source DTO objects.
     */
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

    /**
     * Retrieves a list of all sources.
     *
     * @return a response entity containing a list of all sources
     */
    @GetMapping("/sources")
    public ResponseEntity<?> index() {
        Iterable<Source> sourceIter = sourceRepository.findAll();
        ArrayList<SourceDTO> res = new ArrayList<SourceDTO>();

        for (Source source : sourceIter) {
            res.add(source.toDTO());
        }

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    /**
     * Retrieves a specific source by its ID.
     *
     * @param id the ID of the source to retrieve
     * @return a response entity containing the source, or a 404 error
     */
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

    /**
     * Retrieves a specific source by its ID.
     *
     * @param id the ID of the source to retrieve
     * @return a response entity containing the source, or a 404 error
     */
    @GetMapping("/sources/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        Source source = sourceRepository.findById(id).orElse(null);
        if (source != null) {
            return ResponseEntity.status(HttpStatus.OK).body(source.toDTO());
        }

        return ResponseEntity.status(404).body(null);
    }

    /**
     * Updates a specific source identified by its ID using the provided updates.
     *
     * @param id      the ID of the source to be updated
     * @param updates a map containing the fields to be updated and their new values
     * @return a response entity with the updated source or an error message
     */
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
