package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Integer> {
    List<Assistance> findByPublicityId(Integer publicityId);

    List<Assistance> findByUserId(Integer userId);

    boolean existsByPublicityIdAndUserId(Integer publicityId, Integer userId);
}
