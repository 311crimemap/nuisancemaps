package com.quirkshop.nuisancemaps.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
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

    @GetMapping("/categories/{id}")
    public ResponseEntity<?> get(@PathVariable(value = "id") final int id) {
        JSendDTO jSendDTO;
        Category category = categoryRepository.findById(id).orElse(null);

        if (category == null) {
            jSendDTO = new JSendDTO("Not Found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jSendDTO);
        }

        jSendDTO = new JSendDTO<Category>("success", category);
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

    // curl localhost:8080/categories
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/categories")
    public ResponseEntity<?> getIndex() {
        Iterable<Category> categoriesIter = categoryRepository.findAllByOrderByIdAsc();
        JSendDTO<Iterable<Category>> jSendDTO = new JSendDTO<Iterable<Category>>("success",
                categoriesIter);
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

    // curl -H 'content-type:application/json' -X POST -d
    // @src/main/resources/data/classifier_categories.json localhost:8080/categories
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @PostMapping("/categories")
    public ResponseEntity<?> create(@RequestBody CategoryGroupDTO categoryGroupDTO) {
        int numCreated = categoryService.createCategoriesDTO(categoryGroupDTO);
        HashMap<String, Integer> response = new HashMap<String, Integer>();
        response.put("numCreated", numCreated);
        JSendDTO<HashMap<String, Integer>> jSendDTO = new JSendDTO<HashMap<String, Integer>>(
                "success", response);
        return ResponseEntity.ok().body(jSendDTO);
    }

    //
    // curl-H'content-type:application/json' -X POST -d '{"dataType":"crime",
    // "text":"test", "label": "16"}' localhost:8080/categories/203
    //
    // or for standalone submit with random non-existent parentId:
    //
    // curl -H 'content-type: application/json' -X POST -d '{"dataType":"crime",
    // "text":"test", "label": "16"}' localhost:8080/categories/0
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @PostMapping("/categories/{parentId}")
    public ResponseEntity<?> create(@PathVariable(value = "parentId") final int parentId,
            @RequestBody Category jsonCategory) {

        Category parent = categoryRepository.findById(parentId).orElse(null);

        Category category = new Category(jsonCategory.getDataType(),
                jsonCategory.getText(),
                jsonCategory.getLabel(),
                parent,
                jsonCategory.getIconName(),
                jsonCategory.getIconUnicode());

        JSendDTO<Category> jSendDTO = new JSendDTO<Category>("success", category);
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            jSendDTO.setStatus("error");
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        return ResponseEntity.ok().body(jSendDTO);
    }

    // curl -X DELETE localhost:8080/categories/<id>
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") final int id) {

        HashMap<String, String> response = new HashMap<String, String>();
        response.put("numDeleted", "0");

        JSendDTO<HashMap<String, String>> jSendDTO = new JSendDTO<HashMap<String, String>>("success",
                response);

        if (categoryRepository.existsById(id)) {
            try {
                categoryRepository.deleteById(id);
                response.put("numDeleted", "1");
                jSendDTO.setData(response);
                return ResponseEntity.ok().body(jSendDTO);
            } catch (Exception e) {
                log.error(e.getMessage());
                jSendDTO.setStatus("error");
                return ResponseEntity.badRequest().body(jSendDTO);
            }
        }

        jSendDTO.setStatus("error");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jSendDTO);
    }

    // curl -H "content-type: application/json" -X PATCH -d '{"text":"hello"}'
    // localhost:8080/categories/244
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @PatchMapping("/categories/{id}")
    public ResponseEntity<?> patch(@PathVariable(value = "id") final int id,
            @RequestBody Category jsonCategory) {

        JSendDTO<Category> jSendDTO = new JSendDTO<Category>("success", null);
        Optional<Category> category = categoryRepository.findById(id);

        if (!category.isPresent())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");

        final Category finalCategory = category.get();

        ReflectionUtils.doWithFields(Category.class, field -> {
            field.setAccessible(true);
            Object value = field.get(jsonCategory);
            if (value != null) {
                field.set(finalCategory, value);
            }
        });

        try {
            Category res = categoryRepository.save(finalCategory);
            jSendDTO.setData(res);
        } catch (Exception e) {
            log.error(e.getMessage());
            jSendDTO.setStatus("error");
            return ResponseEntity.badRequest().body(jSendDTO);
        }

        jSendDTO.setData(finalCategory);
        return ResponseEntity.ok().body(jSendDTO);

    }

}
