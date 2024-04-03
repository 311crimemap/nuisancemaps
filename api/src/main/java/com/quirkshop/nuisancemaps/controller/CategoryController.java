package com.quirkshop.nuisancemaps.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    //curl localhost:8080/categories
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

    //
    //curl-H'content-type:application/json'  -X POST -d '{"dataType":"crime", "text":"test", "label": "16"}' localhost:8080/categories/203
    //
    // or for standalone submit with random non-existent parentId:
    //
    // curl -H 'content-type: application/json' -X POST -d '{"dataType":"crime",
    // "text":"test", "label": "16"}' localhost:8080/categories/0

    @PostMapping("/categories/{parentId}")
    public ResponseEntity<?> create(@PathVariable(value = "parentId") final int parentId,
                                    @RequestBody Category jsonCategory) {

        Category parent = categoryRepository.findById(parentId).orElse(null);

        Category category = new Category(jsonCategory.getDataType(),
                                         jsonCategory.getText(),
                                         jsonCategory.getLabel(),
                                         parent);


        category = categoryRepository.save(category);

        return ResponseEntity.ok().body(category);
    }

    // curl -X DELETE localhost:8080/categories/<id>
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") final int id) {
        Map<String, String> response = new HashMap<String, String>();
        response.put("numDeleted", "0");

        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            response.put("numDeleted", "1");
            return ResponseEntity.ok().body(response);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // curl -H "content-type: application/json" -X PATCH -d '{"text":"hello"}'
    // localhost:8080/categories/244
    @PatchMapping("/categories/{id}")
    public ResponseEntity<?> patch(@PathVariable(value="id") final int id,
                                   @RequestBody Category jsonCategory) {
        Map<String, String> response = new HashMap<String, String>();


        Optional<Category> category = categoryRepository.findById(id);

        if(category.isPresent()) {
            Category c = category.get();

            if (!jsonCategory.getText().isBlank())
                c.setText(jsonCategory.getText());

            if (jsonCategory.getLabel() != null)
                c.setLabel(jsonCategory.getLabel());

            c = categoryRepository.save(c);

            return ResponseEntity.ok().body(c);
        }

        response.put("status", "Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}
