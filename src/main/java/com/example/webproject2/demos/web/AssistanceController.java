package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/assistance")
@Getter
@Setter
public class AssistanceController {
    @Autowired
    private PublicityService publicityService;

    @Autowired
    private AssistanceService assistanceService;

    @Autowired
    private AssistanceSuccessService assistanceSuccessService;


    @PostMapping("/update")
    public ResponseEntity<?> respondToAssistanceRequest(
            @RequestHeader(value = "token", required = false) String headerToken, // token 可选
            @RequestBody Map<String, Object> requestPayload) {
        try {
            // 获取 token：优先从 Header 获取，若没有则从 Body 获取
            String token = headerToken != null ? headerToken : (String) requestPayload.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Token is missing"));
            }

            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Invalid token"));
            }

            // 获取请求参数
            Integer assistanceId = (Integer) requestPayload.get("assistance_id");
            Integer action = (Integer) requestPayload.get("action"); // 1: 接受, 2: 拒绝

            if (assistanceId == null || action == null || !(action == 1 || action == 2)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "error", "message", "Invalid parameters"));
            }

            // 查询助力请求
            Assistance assistance = assistanceService.getAssistanceById(assistanceId);
            if (assistance == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Assistance not found"));
            }

            // 验证是否是该用户发布的宣传信息
            Publicity publicity = publicityService.getPublicityById(assistance.getPublicityId());
            if (publicity == null || !publicity.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error", "message", "Unauthorized action"));
            }

            // 更新助力状态
            assistance.setStatus(action); // 1: 接受, 2: 拒绝
            assistanceService.updateAssistance(assistance);

            // 如果是“接受”，则将记录插入到 `assistance_success` 表
            if (action == 1) {
                AssistanceSuccess successRecord = new AssistanceSuccess();
                successRecord.setPublicityId(publicity.getPublicityId());
                successRecord.setPublicityUserId(userId);
                successRecord.setAssistanceId(assistance.getAssistanceId());
                successRecord.setAssistanceUserId(assistance.getUserId());
                successRecord.setAcceptedAt(LocalDateTime.now());
                assistanceSuccessService.saveAssistanceSuccess(successRecord);
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Assistance request updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
