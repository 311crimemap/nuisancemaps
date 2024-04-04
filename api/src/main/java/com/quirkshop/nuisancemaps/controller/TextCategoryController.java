package com.quirkshop.nuisancemaps.controller;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.CategoryAPIDTO;
import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.service.TextCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TextCategoryController {

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @Autowired
    TextCategoryService textCategoryService;

    // curl -H 'content-type:application/json' -X POST -d '[{"dataType": "crime",
    // "text":"Animal bite rawr",
    // "label": 0}]' localhost:8080/textcategories

    @PostMapping("/textcategories")
    public ResponseEntity<?> create(@RequestBody List<TextLabelDTO> textLabelDTOs) {
        HashMap<String, Iterable<TextCategory>> response = new HashMap<String, Iterable<TextCategory>>();
        CategoryAPIDTO<HashMap<String, Iterable<TextCategory>>> categoryAPIDTO;

        try {
            Iterable<TextCategory> res = textCategoryService.createTextCategories(textLabelDTOs);
            response.put("data", res);
            categoryAPIDTO = new CategoryAPIDTO<HashMap<String, Iterable<TextCategory>>>("success", response);
        } catch (Exception e) {
            log.error(e.getMessage());
            HashMap<String, String> error = new HashMap<String, String>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(new CategoryAPIDTO<HashMap<String, String>>("error", error));
        }

        return ResponseEntity.ok().body(categoryAPIDTO);
    }

}
