package com.example.webproject2.demos.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/towns")
@CrossOrigin
public class TownController {

    private final TownService townService;

    public TownController(TownService townService) {
        this.townService = townService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTowns() {
        List<Towns> towns = townService.getAllTowns();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", towns);

        return ResponseEntity.ok(response);
    }
}