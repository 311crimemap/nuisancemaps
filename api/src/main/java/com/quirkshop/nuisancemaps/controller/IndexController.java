package com.quirkshop.nuisancemaps.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quirkshop.nuisancemaps.model.TestRecord;

@RestController
public class IndexController {

    @GetMapping("/")
    public TestRecord testopresto() {
        return new TestRecord(1L, "hello");
    }

}
