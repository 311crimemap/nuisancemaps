package com.quirkshop.nuisancemaps.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.CategoryAPIDTO;
import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.service.TextCategoryService;

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

    // curl -H 'content-type:application/json' -X POST -d '[{"dataType": "crime",
    // "text":"Animal bite rawr",
    // "label": 0}]' localhost:8080/textcategories

    @PostMapping("/textcategories")
    public ResponseEntity<?> create(@RequestBody List<TextLabelDTO> textLabelDTOs) {
        CategoryAPIDTO<List<TextCategory>> categoryAPIDTO;

        try {
            List<TextCategory> res = textCategoryService.createTextCategories(textLabelDTOs);
            if (res.size() > 0) {
                categoryAPIDTO = new CategoryAPIDTO<List<TextCategory>>("success", res);
            } else {
                categoryAPIDTO = new CategoryAPIDTO<List<TextCategory>>("nothing saved", res);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(new CategoryAPIDTO<String>("error", e.getMessage()));
        }

        return ResponseEntity.ok().body(categoryAPIDTO);
    }

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

        CategoryAPIDTO<Iterable<TextCategory>> categoryAPIDTO = new CategoryAPIDTO<Iterable<TextCategory>>("success",
                textCategoriesIter);

        return ResponseEntity.status(HttpStatus.OK).body(categoryAPIDTO);
    }

    // curl -X DELETE localhost:8080/textcategories/<id>
    @DeleteMapping("/textcategories/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") final int id) {

        if (textCategoryRepository.existsById(id)) {

            try {
                TextCategory tc = textCategoryRepository.findById(id).orElse(null);
                textCategoryRepository.deleteById(id);

                return ResponseEntity.ok().body(new CategoryAPIDTO<TextCategory>("success", tc));
            } catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.badRequest()
                        .body(new CategoryAPIDTO<String>("error", e.getMessage()));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CategoryAPIDTO<String>("Not Found", null));
    }

}
