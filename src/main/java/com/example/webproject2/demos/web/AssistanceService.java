package com.example.webproject2.demos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    // 根据用户ID查询助力请求
    public List<Assistance> getAssistanceByUserId(Integer userId) {
        return assistanceRepository.findByUserIdOrderByUpdatedAt(userId);
    }


    public void saveAssistance(Assistance newAssistance) {
        assistanceRepository.save(newAssistance);
    }

    public Optional<Assistance> findByPublicityIdAndUserId(Integer publicityId, Integer userId) {
        return assistanceRepository.findByPublicityIdAndUserId(publicityId, userId);
    }


    public void deleteAssistanceById(Integer assistanceId) {
        assistanceRepository.deleteById(assistanceId);
    }
}
