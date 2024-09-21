package com.quirkshop.nuisancemaps.controller;

import com.quirkshop.nuisancemaps.dto.JSendDTO;
import com.quirkshop.nuisancemaps.model.PendingTextCategory;
import com.quirkshop.nuisancemaps.repository.PendingTextCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PendingTextCategoryController {

    @Autowired
    PendingTextCategoryRepository pendingTextCategoryRepository;

    // curl -H 'X-API-KEY: <>' localhost:8080/pendingtextcategories
    //
    // curl -H 'X-API-KEY: <>' localhost:8080/pendingtextcategories?type=crime |
    // jq -r '.data[].text'
    //
    // curl -H 'X-API-KEY: <>' localhost:8080/pendingtextcategories?type=311 |
    // jq -r '.data[].text'

    @GetMapping("/pendingtextcategories")
    public ResponseEntity<?> getIndex(
            @RequestParam(name = "type", required = false) String type) {
        final int LIMIT = 100;

        Iterable<PendingTextCategory> pendingTextCategoriesIter = null;

        if (type == null) {
            pendingTextCategoriesIter = pendingTextCategoryRepository.findAllByOrderByIdDesc();
        } else {
            pendingTextCategoriesIter = pendingTextCategoryRepository.findAllByDataTypeOrderByIdDesc(type);
        }

        JSendDTO<Iterable<PendingTextCategory>> jSendDTO = new JSendDTO<Iterable<PendingTextCategory>>("success",
                pendingTextCategoriesIter);

        return ResponseEntity.status(HttpStatus.OK).body(jSendDTO);
    }

}
