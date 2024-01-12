package com.quirkshop.nuisancemaps.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quirkshop.nuisancemaps.model.Test;

@RestController 
public class TestController {
    
    @GetMapping("/test")
    public Test testopresto() {
        return new Test(1L, "hello");
    } 

}
