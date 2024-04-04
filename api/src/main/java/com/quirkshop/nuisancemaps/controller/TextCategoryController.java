package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.service.TextCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TextCategoryController {

    @Autowired
    TextCategoryService textCategoryService;

    // curl -H 'content-type:application/json' -X POST -d '[{"dataType": "crime",
    // "text":"Animal bite rawr",
    // "label": 0}]' localhost:8080/textcategories

    @PostMapping("/textcategories")
    public ResponseEntity<?> create(@RequestBody List<TextLabelDTO> textLabelDTOs) {
        int res = textCategoryService.createTextCategories(textLabelDTOs);

        return ResponseEntity.ok().body(res);
    }

}
