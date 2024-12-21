package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


    // 接受或拒绝助力请求
    @PostMapping("/update")
    public ResponseEntity<?> respondToAssistanceRequest(
            @RequestHeader(value = "token", required = false) String headerToken, // token 可选
            @RequestBody Map<String, Object> requestPayload) {
        try {
            // 获取 token：优先从 Header 获取，若没有则从 Body 获取
            String token = headerToken != null ? headerToken : (String) requestPayload.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }

            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 获取请求参数
            Integer assistanceId = (Integer) requestPayload.get("assistance_id");
            Integer action = (Integer) requestPayload.get("action"); // 1: 接受, 2: 拒绝

            if (assistanceId == null || action == null || !(action == 1 || action == 2)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid parameters"));
            }

            // 查询助力请求
            Assistance assistance = assistanceService.getAssistanceById(assistanceId);
            if (assistance == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Assistance not found"));
            }

            // 如果是接受操作，检查是否已存在成功记录，避免重复同意
            if (action == 1) {
                boolean alreadyAccepted = assistanceSuccessService
                        .existsByAssistanceIdAndAssistanceUserId(assistanceId, assistance.getUserId());
                if (alreadyAccepted) {
                    return ResponseEntity.ok(Map.of("status", "error", "message", "Assistance request already accepted"));
                }
            }

            // 验证是否是该用户发布的宣传信息
            Publicity publicity = publicityService.getPublicityById(assistance.getPublicityId());
            if (publicity == null || !publicity.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Unauthorized action"));
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
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    // 获取用户自己的助力信息
    @GetMapping("/my")
    public ResponseEntity<?> getMyAssistances(@RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }
            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 查询用户自己的助力信息
            List<Assistance> assistanceList = assistanceService.getAssistanceByUserId(userId);

            List<Map<String, Object>> responseData = new ArrayList<>();
            for (Assistance assistance : assistanceList) {
                Map<String, Object> assistanceData = new HashMap<>();
                assistanceData.put("assistance_id", assistance.getAssistanceId());
                assistanceData.put("publicity_id", assistance.getPublicityId());
                assistanceData.put("description", assistance.getDescription());
                assistanceData.put("image_url", assistance.getImageUrl());
                assistanceData.put("video_url", assistance.getVideoUrl());
                assistanceData.put("status", assistance.getStatus());
                assistanceData.put("created_at", assistance.getCreatedAt());
                assistanceData.put("updated_at", assistance.getUpdatedAt());
                responseData.add(assistanceData);
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // 创建助力请求
    @PostMapping("/create")
    public ResponseEntity<?> createAssistanceRequest(
            @RequestHeader(value = "token", required = false) String headerToken,
            @RequestBody Map<String, Object> requestPayload) {
        try {
            // 获取 token：优先从 Header 获取，若没有则从 Body 获取
            String token = headerToken != null ? headerToken : (String) requestPayload.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }

            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 获取请求参数
            Integer publicityId = (Integer) requestPayload.get("publicity_id");
            String description = (String) requestPayload.get("description");
            String imageUrl = (String) requestPayload.getOrDefault("image_url", null);
            String videoUrl = (String) requestPayload.getOrDefault("video_url", null);

            // 校验必填参数
            if (publicityId == null || description == null || description.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Missing required parameters"));
            }

            // 检查宣传信息是否存在
            Publicity publicity = publicityService.getPublicityById(publicityId);
            if (publicity == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Publicity not found"));
            }

            // 检查是否尝试助力自己的宣传信息
            if (publicity.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Cannot assist your own publicity"));
            }

            // 检查是否已经存在相同的助力请求
            boolean assistanceExists = assistanceService.existsByPublicityIdAndUserId(publicityId, userId);
            if (assistanceExists) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Assistance request already exists"));
            }

            // 构建助力对象
            Assistance newAssistance = new Assistance();
            newAssistance.setPublicityId(publicityId);
            newAssistance.setUserId(userId);
            newAssistance.setDescription(description);
            newAssistance.setImageUrl(imageUrl);
            newAssistance.setVideoUrl(videoUrl);
            newAssistance.setStatus(3); // 初始状态为 3: 取消（还未发布出去）
            newAssistance.setCreatedAt(LocalDateTime.now());
            newAssistance.setUpdatedAt(LocalDateTime.now());

            // 保存助力请求
            assistanceService.saveAssistance(newAssistance);

            // 返回详细的助力信息
            Map<String, Object> responseData = Map.of(
                    "assistance_id", newAssistance.getAssistanceId(),
                    "publicity_id", newAssistance.getPublicityId(),
                    "description", newAssistance.getDescription(),
                    "image_url", newAssistance.getImageUrl(),
                    "video_url", newAssistance.getVideoUrl(),
                    "status", newAssistance.getStatus(),
                    "created_at", newAssistance.getCreatedAt(),
                    "updated_at", newAssistance.getUpdatedAt()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Assistance request created successfully",
                    "data", responseData
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    // 修改助力信息（可发布接口）
    @PostMapping("/modify")
    public ResponseEntity<?> modifyAssistance(
            @RequestHeader(value = "token", required = false) String headerToken,
            @RequestBody Map<String, Object> requestPayload) {
        try {
            // 获取 token 并验证用户身份
            String token = headerToken != null ? headerToken : (String) requestPayload.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }

            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 参数获取
            Integer assistanceId = (Integer) requestPayload.get("assistance_id");
            String description = (String) requestPayload.get("description");
            String imageUrl = (String) requestPayload.get("image_url");
            String videoUrl = (String) requestPayload.get("video_url");
            Integer status = (Integer) requestPayload.getOrDefault("status", null);

            if (assistanceId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Missing assistance_id"));
            }

            // 查找助力信息
            Assistance assistance = assistanceService.getAssistanceById(assistanceId);
            if (assistance == null || !assistance.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Assistance not found or unauthorized"));
            }

            // 检查助力状态，只有未被同意的（状态为 0 或 2 或 3）才能修改
            if (assistance.getStatus() != 0 && assistance.getStatus() != 3 && assistance.getStatus() != 2) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Cannot modify accepted assistance"));
            }

            // 更新字段
            if (description != null) assistance.setDescription(description);
            if (imageUrl != null) assistance.setImageUrl(imageUrl);
            if (videoUrl != null) assistance.setVideoUrl(videoUrl);
            if (status != null) assistance.setStatus(status);
            assistance.setUpdatedAt(LocalDateTime.now());

            assistanceService.updateAssistance(assistance);

            // 构建详细响应信息
            Map<String, Object> responseData = Map.of(
                    "assistance_id", assistance.getAssistanceId(),
                    "publicity_id", assistance.getPublicityId(),
                    "user_id", assistance.getUserId(),
                    "description", assistance.getDescription(),
                    "image_url", assistance.getImageUrl(),
                    "video_url", assistance.getVideoUrl(),
                    "status", assistance.getStatus(),
                    "created_at", assistance.getCreatedAt(),
                    "updated_at", assistance.getUpdatedAt()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Assistance modified successfully",
                    "data", responseData
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    // 删除助力信息
    @DeleteMapping("/delete/{assistanceId}")
    public ResponseEntity<?> deleteAssistance(
            @RequestHeader(value = "token", required = false) String headerToken,
            @PathVariable("assistanceId") Integer assistanceId) {
        try {
            // 获取 token 并验证用户身份
            String token = headerToken;
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }

            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 查找助力信息
            Assistance assistance = assistanceService.getAssistanceById(assistanceId);
            if (assistance == null || !assistance.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Assistance not found or unauthorized"));
            }

            // 检查助力状态，只有未被接受的（状态为 0 或 2 或 3 ）才能删除
            if (assistance.getStatus() != 0 && assistance.getStatus() != 2 && assistance.getStatus() != 3) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Cannot delete accepted assistance"));
            }

            // 删除助力记录
            assistanceService.deleteAssistanceById(assistanceId);

            return ResponseEntity.ok(Map.of("status", "success", "message", "Assistance deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


}
