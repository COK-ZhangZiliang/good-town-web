package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.webproject2.demos.web.Towns;

public interface TownRepository extends JpaRepository<Towns, Integer> {
}
