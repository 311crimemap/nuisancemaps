package com.quirkshop.nuisancemaps.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;

@RestController
public class DataJobController {

    @Autowired
    DataJobRepository dataJobRepository;

    @GetMapping("/datajobs")
    public List<DataJob> getIndex() {
        List<DataJob> dataJobs = dataJobRepository.findAllByOrderByIdDesc();

        return dataJobs;
    }

}
