package com.example.webproject2.demos.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssistanceSuccessService {
    @Autowired
    private AssistanceSuccessRepository assistanceSuccessRepository;

    public void saveAssistanceSuccess(AssistanceSuccess success) {
        assistanceSuccessRepository.save(success);
    }

    public boolean existsByAssistanceIdAndAssistanceUserId(Integer assistanceId, Integer assistanceUserId) {
        // 数据库查询，验证是否已存在对应记录
        return assistanceSuccessRepository.existsByAssistanceIdAndAssistanceUserId(assistanceId, assistanceUserId);
    }

}
