package com.quirkshop.nuisancemaps.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.GeometryDTO;
import com.quirkshop.nuisancemaps.dto.InitDTO;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.LocaleDTO;
import com.quirkshop.nuisancemaps.dto.LocaleFeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.LocaleFeatureDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.LocaleCategoryMinMaxReportedAt;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.LocaleCategoryMinMaxReportedAtRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitController {

    @Autowired
    LocaleRepository localeRepository;

    @Autowired
    LocaleCategoryMinMaxReportedAtRepository localeCategoryMinMaxReportedAtRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    /**
     * Initial display data needed for front-end web: required locales
     * (city/states) and categories (dropdown)
     *
     * @return ResponseEntity<?> A response entity containing a JSendDTO object with
     *         status "success"
     *         and the initialization data, or a bad request response in case of an
     *         error.
     */
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/init")
    public ResponseEntity<?> index() {
        JSendDTO jSendDTO;

        try {

            List<Locale> locales = localeRepository.findAll();
            List<LocaleCategoryMinMaxReportedAt> localeCategoryMinMaxReportedAts = localeCategoryMinMaxReportedAtRepository
                    .findAll();

            Map<Integer, List<LocaleCategoryMinMaxReportedAt>> localeCategoryMinMaxReportedAtMap = localeCategoryMinMaxReportedAts
                    .stream()
                    .collect(Collectors.groupingBy(LocaleCategoryMinMaxReportedAt::getLocaleId));

            ArrayList<LocaleFeatureDTO> localeFeatureDTOs = new ArrayList<LocaleFeatureDTO>();

            List<Category> categories = categoryRepository.findAllByTextNotOrderByIdAsc("SKIP");

            for (Locale locale : locales) {

                Double[] location = { locale.getLocation().getX(), locale.getLocation().getY() };
                GeometryDTO g = new GeometryDTO("Point", location);

                List<LocaleCategoryMinMaxReportedAt> categoryMinMaxReportedAt = localeCategoryMinMaxReportedAtMap
                        .get(locale.getId());
                LocaleDTO localeDTO = locale.toDTO(categoryMinMaxReportedAt);

                LocaleFeatureDTO localeFeatureDTO = new LocaleFeatureDTO("Feature", g, localeDTO);
                localeFeatureDTOs.add(localeFeatureDTO);
            }

            LocaleFeatureCollectionDTO localeFeatureCollectionDTO = new LocaleFeatureCollectionDTO("FeatureCollection",
                    localeFeatureDTOs);

            InitDTO initDTO = new InitDTO(localeFeatureCollectionDTO, categories);

            jSendDTO = new JSendDTO<InitDTO>("success", initDTO);

        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            jSendDTO = new JSendDTO<String>("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

}
