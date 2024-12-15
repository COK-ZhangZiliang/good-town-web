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
}
