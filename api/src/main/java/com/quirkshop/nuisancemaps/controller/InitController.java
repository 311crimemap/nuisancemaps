package com.quirkshop.nuisancemaps.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.dto.InitDTO;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.SourceDTO;

@RestController
public class InitController {

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/init")
    public ResponseEntity<?> index() {
        JSendDTO jSendDTO;

        try {
            ArrayList<SourceDTO> sourceDTOs = new ArrayList<SourceDTO>();
            Iterable<Source> sources = sourceRepository.findAll();
            List<Category> categories = categoryRepository.findAllByTextNotOrderByIdAsc("SKIP");

            for (Source source : sources) {
                Double[] location = { source.getLocation().getX(), source.getLocation().getY() };
                SourceDTO sourceDTO = new SourceDTO(source.getSourceConfigId(),
                        source.getSourceConfigEntity(),
                        source.getSourceConfigNotes(),
                        location,
                        source.getIconName(),
                        source.getIconUnicode(),
                        source.getCategory(),
                        source.getDescription(),
                        source.getUrl(),
                        source.getNumRecords());

                sourceDTOs.add(sourceDTO);
            }

            InitDTO initDTO = new InitDTO(sourceDTOs, categories);
            jSendDTO = new JSendDTO("success", initDTO);

        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

}
