package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TownRepository extends JpaRepository<Towns, Integer> {
    Towns findByProvinceAndCityAndTownName(String province, String city, String townName);
}