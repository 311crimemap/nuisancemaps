package com.quirkshop.nuisancemaps.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.LocaleDTO;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.SourceLoaderService;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * Retrieves a Locale by its ID.
     *
     * @param id the ID of the Locale to retrieve
     * @return a ResponseEntity containing the Locale DTO if found,
     *         or a 404 status if not found
     */
    @GetMapping("/locales/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        Locale locale = localeRepository.findById(id).orElse(null);
        if (locale != null) {
            return ResponseEntity.status(HttpStatus.OK).body(locale.toDTO());
        }
        return ResponseEntity.status(404).body(null);
    }

    /**
     * Retrieves all Locales.
     *
     * @return a ResponseEntity containing a list of all Locale DTOs
     *         with a status of OK
     */
    @GetMapping("/locales")
    public ResponseEntity<?> index() {
        Iterable<Locale> localeIter = localeRepository.findAll();
        List<LocaleDTO> localeDTOs = new ArrayList<LocaleDTO>();

        for (Locale locale : localeIter) {
            localeDTOs.add(locale.toDTO());
        }

        return ResponseEntity.status(HttpStatus.OK).body(localeDTOs);
    }

    /**
     * Creates a new Locale.
     *
     * @param locale the Locale object to create
     * @return a ResponseEntity containing a JSendDTO with status "success"
     *         and the created Locale DTO, or a JSendDTO with status "error"
     *         if the creation fails
     */
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

    /**
     * Creates a batch of new Locales.
     *
     * @param locales a list of Locale objects to create
     * @return a ResponseEntity containing a JSendDTO with status "success"
     *         and a list of created Locale DTOs, or a JSendDTO with status "error"
     *         if the batch creation fails
     */
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

    /**
     * Updates an existing Locale by its ID.
     *
     * @param id        the ID of the Locale to update
     * @param localeDTO the LocaleDTO containing the updated fields
     * @return a ResponseEntity containing a JSendDTO with status "success"
     *         and the updated Locale DTO, or a JSendDTO with status "error"
     *         if the update fails
     */

    @PatchMapping("/locales/{id}")
    public ResponseEntity<?> update(@PathVariable(value = "id") final int id,
            @RequestBody LocaleDTO localeDTO) {
        JSendDTO jSendDTO;
        Locale locale = null;
        try {
            locale = localeRepository.findById(id).orElse(null);

            if (locale == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Field[] fields = LocaleDTO.class.getDeclaredFields();

            for (Field localeDTOField : fields) {
                localeDTOField.setAccessible(true);
                Object value = localeDTOField.get(localeDTO);

                if (value == null)
                    continue;

                Field localeField = Locale.class.getDeclaredField(localeDTOField.getName());
                localeField.setAccessible(true);

                // Locale location member is of dataType Point - needs to be
                // converted from JSON (x,y) values.
                //
                // Other points are primitives, and can be set as-is.
                if (localeDTOField.getName().equals("location")) {
                    double x = localeDTO.getLocation()[0];
                    double y = localeDTO.getLocation()[1];
                    value = geometryFactory.createPoint(new Coordinate(x, y));
                }

                localeField.set(locale, value);

            }

            locale = localeRepository.save(locale);

        } catch (Exception e) {
            e.printStackTrace();
            jSendDTO = new JSendDTO("error", e.getMessage());
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        jSendDTO = new JSendDTO("success", locale.toDTO());
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);

    }

}
