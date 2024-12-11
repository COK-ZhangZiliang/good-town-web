package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicityRepository extends JpaRepository<Publicity, Integer> {
    List<Publicity> findByUserIdAndStatus(Integer userId, Integer status);


}


