package com.example.webproject2.demos.web;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TownService {

    private final TownRepository townRepository;

    public TownService(TownRepository townRepository) {
        this.townRepository = townRepository;
    }

    public List<Towns> getAllTowns() {
        return townRepository.findAll();
    }
}