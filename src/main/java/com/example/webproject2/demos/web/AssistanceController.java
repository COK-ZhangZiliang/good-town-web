package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/assistance")
@Getter
@Setter
@CrossOrigin
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

                // 处理 image_url 字段，分割为数组形式
                String rawImageUrls = assistance.getImageUrl();
                if (rawImageUrls != null && !rawImageUrls.trim().isEmpty()) {
                    List<String> parsedImageUrls = Arrays.asList(rawImageUrls.split(";"));
                    assistanceData.put("image_url", parsedImageUrls); // 转为列表形式
                } else {
                    assistanceData.put("image_url", Collections.emptyList()); // 空列表
                }

                // 处理 video_url 字段，分割为数组形式
                String rawVideoUrls = assistance.getVideoUrl();
                if (rawVideoUrls != null && !rawVideoUrls.trim().isEmpty()) {
                    List<String> parsedVideoUrls = Arrays.asList(rawVideoUrls.split(";"));
                    assistanceData.put("video_url", parsedVideoUrls); // 转为列表形式
                } else {
                    assistanceData.put("video_url", Collections.emptyList()); // 空列表
                }

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
            List<String> imageUrlList = (List<String>) requestPayload.get("image_url"); // 前端传递的 image_url 数组
            List<String> videoUrlList = (List<String>) requestPayload.get("video_url"); // 前端传递的 video_url 数组

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

            // 将 imageUrlList 和 videoUrlList 转为以 `;` 分隔的字符串
            if (imageUrlList != null && !imageUrlList.isEmpty()) {
                String imageUrl = String.join(";", imageUrlList); // 使用 ; 连接
                newAssistance.setImageUrl(imageUrl);
            } else {
                newAssistance.setImageUrl(null); // 如果为空，保存为 null
            }

            if (videoUrlList != null && !videoUrlList.isEmpty()) {
                String videoUrl = String.join(";", videoUrlList); // 使用 ; 连接
                newAssistance.setVideoUrl(videoUrl);
            } else {
                newAssistance.setVideoUrl(null); // 如果为空，保存为 null
            }

            newAssistance.setStatus(3); // 初始状态为 3: 取消（还未发布出去）
            newAssistance.setCreatedAt(LocalDateTime.now());
            newAssistance.setUpdatedAt(LocalDateTime.now());

            // 保存助力请求
            assistanceService.saveAssistance(newAssistance);

            // 修复后的代码，使用 HashMap 构建返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("assistance_id", newAssistance.getAssistanceId());
            responseData.put("publicity_id", newAssistance.getPublicityId());
            responseData.put("description", newAssistance.getDescription());
            responseData.put("image_url", newAssistance.getImageUrl());
            responseData.put("video_url", newAssistance.getVideoUrl());
            responseData.put("status", newAssistance.getStatus());
            responseData.put("created_at", newAssistance.getCreatedAt());
            responseData.put("updated_at", newAssistance.getUpdatedAt());

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
            List<String> imageUrlList = (List<String>) requestPayload.get("image_url"); // 前端传递的 image_url 数组
            List<String> videoUrlList = (List<String>) requestPayload.get("video_url"); // 前端传递的 video_url 数组
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

            // 将 imageUrlList 和 videoUrlList 转为以 `;` 分隔的字符串
            if (imageUrlList != null && !imageUrlList.isEmpty()) {
                String imageUrl = String.join(";", imageUrlList); // 使用 ; 连接
                assistance.setImageUrl(imageUrl);
            } else {
                assistance.setImageUrl(null); // 如果为空，保存为 null
            }

            if (videoUrlList != null && !videoUrlList.isEmpty()) {
                String videoUrl = String.join(";", videoUrlList); // 使用 ; 连接
                assistance.setVideoUrl(videoUrl);
            } else {
                assistance.setVideoUrl(null); // 如果为空，保存为 null
            }

            if (status != null) assistance.setStatus(status);
            assistance.setUpdatedAt(LocalDateTime.now());

            assistanceService.updateAssistance(assistance);

            // 构建详细响应信息
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("assistance_id", assistance.getAssistanceId());
            responseData.put("publicity_id", assistance.getPublicityId());
            responseData.put("user_id", assistance.getUserId());
            responseData.put("description", assistance.getDescription());
            responseData.put("image_url", assistance.getImageUrl());
            responseData.put("video_url", assistance.getVideoUrl());
            responseData.put("status", assistance.getStatus());
            responseData.put("created_at", assistance.getCreatedAt());
            responseData.put("updated_at", assistance.getUpdatedAt());

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
