package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicityService {
    @Autowired
    private PublicityRepository publicityRepository;


    public List<Publicity> getMyPublicity(Integer userId) {
        // 查询状态为已发布的宣传信息 (status = 0)
        return publicityRepository.findByUserIdAndStatus(userId, 0);
    }

}
