package com.quirkshop.nuisancemaps.controller;

import java.util.HashMap;
import java.util.Optional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.service.CategoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryService categoryService;

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return ResponseEntity containing the category data or a "Not Found" message
     *         if the category does not exist
     */
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

    /**
     * Retrieves all categories, excluding those marked with "SKIP".
     *
     * @return ResponseEntity containing a list of categories
     */
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/categories")
    public ResponseEntity<?> getIndex() {
        Iterable<Category> categoriesIter = categoryRepository.findAllByTextNotOrderByIdAsc("SKIP");
        JSendDTO<Iterable<Category>> jSendDTO = new JSendDTO<Iterable<Category>>("success",
                categoriesIter);
        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

    /**
     * Batch Create multiple categories.
     *
     * curl -H 'content-type:application/json' -X POST -d @classifier_categories.json  \
     * localhost:8080/categories
     *
     * @param categoryGroupDTO the data transfer object containing the categories to
     *                         create
     * @return ResponseEntity indicating the number of categories created
     */
    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @PostMapping("/categories")
    public ResponseEntity<?> createBatch(@RequestBody CategoryGroupDTO categoryGroupDTO) {
        int numCreated = categoryService.createCategoriesDTO(categoryGroupDTO);
        HashMap<String, Integer> response = new HashMap<String, Integer>();
        response.put("numCreated", numCreated);
        JSendDTO<HashMap<String, Integer>> jSendDTO = new JSendDTO<HashMap<String, Integer>>(
                "success", response);
        return ResponseEntity.ok().body(jSendDTO);
    }


    /**
     * Creates a new category under a specified parent category ID.
     *
     * curl-H'content-type:application/json' -X POST -d '{"dataType":"crime",
     * "text":"test", "label": "16"}' localhost:8080/categories/203
     *
     * or for standalone submit with random non-existent parentId:
     *
     * curl -H 'content-type: application/json' -X POST -d '{"dataType":"crime",
     * "text":"test", "label": "16"}' localhost:8080/categories/0
     *
     * @param parentId     the ID of the parent category
     * @param jsonCategory the category object containing data to be saved
     * @return ResponseEntity with the created category data or an error message if
     *         the operation fails
     */
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

    /**
     * Deletes a category by its ID.
     *
     * curl-X DELETE localhost:8080/categories/<id>
     *
     * @param id the ID of the category to delete
     * @return ResponseEntity indicating the success or failure of the deletion
     *         operation
     */
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

    /**
     * Updates a category by its ID with the provided data.
     *
     * curl -H "content-type: application/json" -X PATCH -d '{"text":"hello"}'
     *
     * @param id           the ID of the category to update
     * @param jsonCategory the category object containing the data to update
     * @return ResponseEntity containing the updated category data or an error
     *         message if the operation fails
     */
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
