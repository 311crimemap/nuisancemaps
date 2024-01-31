package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;

@RestController
public class DataJobController {

    @Autowired
    DataJobRepository dataJobRepository;

    @GetMapping("/datajobs")
    public List<DataJob> getIndex(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {

        final int LIMIT = 50;

        if (page != null && limit != null) {
            return dataJobRepository.findAllByOrderByIdDesc(PageRequest.of(page, limit));
        } else if (page != null) {
            return dataJobRepository.findAllByOrderByIdDesc(PageRequest.of(page, LIMIT));
        } else if (limit != null) {
            return dataJobRepository.findAllByOrderByIdDesc(PageRequest.of(0, limit));
        }

        return dataJobRepository.findAllByOrderByIdDesc(PageRequest.of(0, LIMIT));
    }

}
