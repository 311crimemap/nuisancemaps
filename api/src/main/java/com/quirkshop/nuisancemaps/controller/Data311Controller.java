package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.repository.Data311Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Data311Controller {

    @Autowired
    Data311Repository data311Repository;

    @CrossOrigin(origins = "${CORS_ORIGINS}")
    @GetMapping("/data311s")
    public List<Data311> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        if (page != null && limit != null) {
            return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(0, limit));
        }

        return data311Repository.findAllByOrderByReportedAtDesc(PageRequest.of(0, LIMIT));
    }
}
