package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TownService {
    @Autowired
    private final TownRepository townRepository;

    public TownService(TownRepository townRepository) {
        this.townRepository = townRepository;
    }

    public List<Towns> getAllTowns() {
        return townRepository.findAll();
    }

    public Integer findOrCreateTown(String province, String city, String townName) {
        Towns town = townRepository.findByProvinceAndCityAndTownName(province, city, townName);
        if (town == null) {
            // 如果找不到对应的 Town，则创建一个新的
            town = new Towns();
            town.setProvince(province);
            town.setCity(city);
            town.setTownName(townName);
            town = townRepository.save(town); // 保存到数据库
        }
        return town.getTownId();
    }

    public Towns getTownById(Integer townId) {
        // 根据 townId 查询 Town 信息
        return townRepository.findById(townId).orElse(null);
    }

}