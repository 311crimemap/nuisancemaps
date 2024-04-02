package com.quirkshop.nuisancemaps.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryService categoryService;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    //@CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/categories")
    public ResponseEntity<?> getIndex() {
        Iterable<Category> categoriesIter = categoryRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(categoriesIter);
    }

    // curl -H 'content-type:application/json' -X POST -d
    // @src/main/resources/data/classifier_categories.json localhost:8080/categories
    //@CrossOrigin(origins = "${CORS_ORIGINS}")
    @PostMapping("/categories")
    public ResponseEntity<?> create(@RequestBody CategoryGroupDTO categoryGroupDTO) {
        int numCreated = categoryService.createCategoriesDTO(categoryGroupDTO);
        Map<String, String> response = new HashMap<String, String>();
        response.put("numCreated", Integer.toString(numCreated));
        return ResponseEntity.ok().body(response);
    }

}
