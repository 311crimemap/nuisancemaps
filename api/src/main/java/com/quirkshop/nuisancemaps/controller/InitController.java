package com.quirkshop.nuisancemaps.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.InitDTO;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.LocaleDTO;
import com.quirkshop.nuisancemaps.dto.LocaleFeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.LocaleFeatureDTO;

@RestController
public class InitController {

    @Autowired
    LocaleRepository localeRepository;

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
            ArrayList<LocaleFeatureDTO> localeFeatureDTOs = new ArrayList<LocaleFeatureDTO>();
            Iterable<Locale> locales = localeRepository.findAllWithSources();

            List<Category> categories = categoryRepository.findAllByTextNotOrderByIdAsc("SKIP");

            for (Locale locale : locales) {

                Double[] location = { locale.getLocation().getX(), locale.getLocation().getY() };
                GeometryDTO g = new GeometryDTO("Point", location);

                LocaleDTO localeDTO = locale.toDTO();

                LocaleFeatureDTO localeFeatureDTO = new LocaleFeatureDTO("Feature", g, localeDTO);
                localeFeatureDTOs.add(localeFeatureDTO);
            }

            LocaleFeatureCollectionDTO localeFeatureCollectionDTO = new LocaleFeatureCollectionDTO("FeatureCollection",
                    localeFeatureDTOs);

            InitDTO initDTO = new InitDTO(localeFeatureCollectionDTO, categories);

            jSendDTO = new JSendDTO("success", initDTO);

        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

}
