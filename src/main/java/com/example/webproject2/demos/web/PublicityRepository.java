package com.example.webproject2.demos.web;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicityRepository extends JpaRepository<Publicity, Integer> {
    List<Publicity> findByUserIdAndStatus(Integer userId, Integer status);

    List<Publicity> findByUserIdAndStatusInOrderByUpdatedAtDesc(Integer userId, List<Integer> statuses);


    boolean existsByUserIdAndTownId(Integer userId, Integer townId);

    // 按照 updated_at 降序排序（从新到旧）
    List<Publicity> findByStatusOrderByUpdatedAtDesc(Integer status);

}


