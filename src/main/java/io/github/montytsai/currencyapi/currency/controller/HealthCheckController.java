package io.github.montytsai.currencyapi.currency.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public String helloWorld() {
        return "Project is running!";
    }

}