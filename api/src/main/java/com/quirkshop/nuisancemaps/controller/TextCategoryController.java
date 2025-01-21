package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.service.TextCategoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TextCategoryController {

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @Autowired
    TextCategoryRepository textCategoryRepository;

    @Autowired
    TextCategoryService textCategoryService;

    /**
     * Creates new text categories based on the provided list of text labels.
     *
     * curl -H 'content-type:application/json' -X POST \
     * -d '[{"dataType": "crime", "text":"Animal bite rawr", "label": 0}]' \
     * localhost:8080/textcategories
     * 
     * @param textLabelDTOs a list of TextLabelDTO objects representing the text
     *                      categories to be created
     * @return ResponseEntity<?> return status of the operation with the created
     *         TextCategory objects or an error message.
     */
    @PostMapping("/textcategories")
    public ResponseEntity<?> create(@RequestBody List<TextLabelDTO> textLabelDTOs) {
        JSendDTO<List<TextCategory>> jSendDTO;
        textCategoryService.initMaps();

        try {
            List<TextCategory> res = textCategoryService.createTextCategories(textLabelDTOs);
            if (res.size() > 0) {
                jSendDTO = new JSendDTO<List<TextCategory>>("success", res);
            } else {
                jSendDTO = new JSendDTO<List<TextCategory>>("nothing saved", res);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(new JSendDTO<String>("error", e.getMessage()));
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

    /**
     * Retrieves a paginated list of text categories.
     *
     * @param page  the page number to retrieve (optional)
     * @param limit the number of text categories to return per page (optional)
     * @return ResponseEntity<?> a JSendDTO with a list of TextCategory objects
     */
    @GetMapping("/textcategories")
    public ResponseEntity<?> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {
        final int LIMIT = 50;

        Iterable<TextCategory> textCategoriesIter = null;

        if (page != null && limit != null) {
            textCategoriesIter = textCategoryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            textCategoriesIter = textCategoryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            textCategoriesIter = textCategoryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
        } else {
            textCategoriesIter = textCategoryRepository.findAllByOrderByCreatedAtDesc(null);
        }

        JSendDTO<Iterable<TextCategory>> jSendDTO = new JSendDTO<Iterable<TextCategory>>("success",
                textCategoriesIter);

        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

    /**
     * Deletes a text category by its ID.
     *
     * curl -X DELETE localhost:8080/textcategories/<id>
     *
     * @param id the ID of the text category to be deleted
     * @return ResponseEntity<?> indicates status of the delete operation: Success,
     *         Error, Not Found
     */

    @DeleteMapping("/textcategories/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") final int id) {

        if (textCategoryRepository.existsById(id)) {

            try {
                TextCategory tc = textCategoryRepository.findById(id).orElse(null);
                textCategoryRepository.deleteById(id);

                return ResponseEntity.ok().body(new JSendDTO<TextCategory>("success", tc));
            } catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.badRequest()
                        .body(new JSendDTO<String>("error", e.getMessage()));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JSendDTO<String>("Not Found", null));
    }

}
