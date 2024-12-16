package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PublicityService {
    @Autowired
    private PublicityRepository publicityRepository;


    public List<Publicity> getMyPublicity(Integer userId) {
        // 查询状态为已发布的宣传信息 (status = 0) 和取消发布的宣传信息 (status = -1)
        return publicityRepository.findByUserIdAndStatusIn(userId, Arrays.asList(0, -1));
    }

    public List<Publicity> getReleasedPublicity(Integer userId) {
        // 查询状态为已发布的宣传信息 (status = 0)
        return publicityRepository.findByUserIdAndStatus(userId, 0);
    }


    public Publicity addPublicity(Publicity publicity) {
        // 新增宣传信息
        return publicityRepository.save(publicity);
    }


    public boolean existsByUserIdAndTownId(Integer userId, Integer townId) {
        // 查询用户是否已经发布过该乡镇的宣传信息
        return publicityRepository.existsByUserIdAndTownId(userId, townId);
    }


    public Publicity getPublicityById(Integer publicityId) {
        // 根据宣传信息 ID 查询宣传信息
        return publicityRepository.findById(publicityId).orElse(null);
    }


}
