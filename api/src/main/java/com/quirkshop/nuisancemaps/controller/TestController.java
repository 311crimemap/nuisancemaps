package com.quirkshop.nuisancemaps.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.model.Test;
import com.quirkshop.nuisancemaps.repository.TestRepository;

@RestController
public class TestController {
    @Autowired
    private TestRepository testRepository;

    @PostMapping(path = "/test/create") // Map ONLY POST Requests
    public @ResponseBody String create(@RequestParam String name, Integer age) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request
        // curl -X POST -d name=Howdy localhost:8080/test/create
        Test t = new Test(name, age);
        testRepository.save(t);
        return "Saved";
    };

    // NB: terminal slash not included; path is explicit
    // likely defer this handling to nginx
    @GetMapping(path = "/tests")
    public @ResponseBody Iterable<Test> index() {
        return testRepository.findAll();
    };

    @GetMapping(path = "/tests/{id}")
    public @ResponseBody Optional<Test> get(@PathVariable(value = "id") final int id) {
        return testRepository.findById(id);
    };

}
