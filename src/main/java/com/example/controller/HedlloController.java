package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HedlloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

}
