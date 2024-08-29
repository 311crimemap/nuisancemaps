package com.quirkshop.nuisancemaps.controller;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.SourceLoaderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.LocaleDTO;

@RestController
public class LocaleController {

    @Autowired
    MappingRepository mappingRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    LocaleRepository localeRepository;

    @Autowired
    SourceLoaderService sourceLoaderService;

    private final int SRID = 4326;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
            SRID);
    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @GetMapping("/locales/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        Locale locale = localeRepository.findById(id).orElse(null);
        if (locale != null) {
            return ResponseEntity.status(HttpStatus.OK).body(locale.toDTO());
        }
        return ResponseEntity.status(404).body(null);
    }

    @GetMapping("/locales")
    public ResponseEntity<?> index() {
        Iterable<Locale> localeIter = localeRepository.findAll();
        List<LocaleDTO> localeDTOs = new ArrayList<LocaleDTO>();

        for (Locale locale : localeIter) {
            localeDTOs.add(locale.toDTO());
        }

        return ResponseEntity.status(HttpStatus.OK).body(localeDTOs);
    }

    @PostMapping("/locales")
    public ResponseEntity<?> create(@RequestBody Locale locale) {
        JSendDTO jSendDTO;
        try {
            Locale localeSaved = localeRepository.save(locale);
            jSendDTO = new JSendDTO("success", localeSaved.toDTO());

        } catch (Exception e) {
            jSendDTO = new JSendDTO("error", null);
            return ResponseEntity.badRequest().body(jSendDTO);
        }
        return ResponseEntity.ok().body(jSendDTO);
    }

    @PostMapping("/locales/batch")
    public ResponseEntity<?> createBatch(@RequestBody List<Locale> locales) {
        JSendDTO jSendDTO;

        try {

            Iterable<Locale> localesSaved = localeRepository.saveAll(locales);
            List<LocaleDTO> localeDTOs = new ArrayList<LocaleDTO>();

            for (Locale locale : localesSaved) {
                localeDTOs.add(locale.toDTO());
            }

            jSendDTO = new JSendDTO("success", localeDTOs);

        } catch (Exception e) {
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }
        return ResponseEntity.ok().body(jSendDTO);
    }

    @PatchMapping("/locales/{id}")
    public ResponseEntity<?> update(@PathVariable(value = "id") final int id,
            @RequestBody LocaleDTO localeDTO) {
        JSendDTO jSendDTO;
        Locale locale = null;
        try {
            locale = localeRepository.findById(id).orElse(null);

            if (locale != null) {
                Field[] fields = LocaleDTO.class.getDeclaredFields();

                for (Field localeDTOField : fields) {
                    localeDTOField.setAccessible(true);
                    Object value = localeDTOField.get(localeDTO);

                    if (value != null) {
                        Field localeField = Locale.class.getDeclaredField(localeDTOField.getName());
                        localeField.setAccessible(true);

                        if (localeDTOField.getName().equals("location")) {
                            double x = localeDTO.getLocation()[0];
                            double y = localeDTO.getLocation()[1];
                            value = geometryFactory.createPoint(new Coordinate(x, y));
                        }

                        localeField.set(locale, value);
                    }
                }
                locale = localeRepository.save(locale);
            }

        } catch (Exception e) {
            e.printStackTrace();
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        jSendDTO = new JSendDTO("success", locale.toDTO());
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);

    }

}
