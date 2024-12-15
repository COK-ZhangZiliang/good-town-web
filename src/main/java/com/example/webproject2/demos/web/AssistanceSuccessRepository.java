package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistanceSuccessRepository extends JpaRepository<AssistanceSuccess, Integer> {
    // JpaRepository 自动提供 save、findById 等方法
}