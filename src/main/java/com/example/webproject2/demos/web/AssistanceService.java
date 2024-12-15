package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistanceService {
    @Autowired
    private AssistanceRepository assistanceRepository;

    // 根据宣传信息ID查询助力请求
    public List<Assistance> getAssistanceByPublicityId(Integer publicityId) {
        return assistanceRepository.findByPublicityId(publicityId);
    }

    // 根据助力请求ID查询助力请求
    public Assistance getAssistanceById(Integer assistanceId) {
        return assistanceRepository.findById(assistanceId).orElse(null);
    }

    // 更新助力请求
    public void updateAssistance(Assistance assistance) {
        assistanceRepository.save(assistance);
    }

}
