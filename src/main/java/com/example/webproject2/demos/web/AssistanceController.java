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

    @Autowired
    private TownService townService;


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
                return ResponseEntity.ok(Map.of("status", "error", "message", "参数无效"));
            }

            // 查询助力请求
            Assistance assistance = assistanceService.getAssistanceById(assistanceId);
            if (assistance == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "未找到这个助力请求"));
            }

            // 如果是接受操作，检查是否已存在成功记录，避免重复同意
            if (action == 1) {
                boolean alreadyAccepted = assistanceSuccessService
                        .existsByAssistanceIdAndAssistanceUserId(assistanceId, assistance.getUserId());
                if (alreadyAccepted) {
                    return ResponseEntity.ok(Map.of("status", "error", "message", "助力请求已被接受"));
                }
            }

            // 验证是否是该用户发布的宣传信息
            Publicity publicity = publicityService.getPublicityById(assistance.getPublicityId());
            if (publicity == null || !publicity.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "未经授权的行为"));
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

            return ResponseEntity.ok(Map.of("status", "success", "message", "助力请求更新成功"));

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
                if (assistance.getStatus() == 2 || assistance.getStatus() == 3)
                    continue; // 跳过已拒绝和已取消

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

                // 根据 publicityId 查询对应的 townId
                Integer publicityId = assistance.getPublicityId();
                if (publicityId != null) {
                    Publicity publicity = publicityService.getPublicityById(publicityId);
                    if (publicity != null) {
                        Integer townId = publicity.getTownId();
                        assistanceData.put("town_id", townId);

                        // 根据 townId 查询详细的省市和 townName
                        if (townId != null) {
                            Towns town = townService.getTownById(townId); // 正确：通过注入的实例调用
                            if (town != null) {
                                assistanceData.put("province", town.getProvince());
                                assistanceData.put("city", town.getCity());
                                assistanceData.put("town_name", town.getTownName());
                            }
                        }
                    }
                }

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
                return ResponseEntity.ok(Map.of("status", "error", "message", "缺少必要参数"));
            }

            // 检查宣传信息是否存在
            Publicity publicity = publicityService.getPublicityById(publicityId);
            if (publicity == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "未找到宣传信息"));
            }

            // 检查是否尝试助力自己的宣传信息
            if (publicity.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "无法为自己助力"));
            }

            // 检查是否已经存在相同的助力请求
            Optional<Assistance> existingAssistance = assistanceService.findByPublicityIdAndUserId(publicityId, userId);
            if (existingAssistance.isPresent()) {
                Assistance assistance = existingAssistance.get();
                // 如果状态不是拒绝状态或删除状态（status=2表示拒绝）
                if (assistance.getStatus() != 2 && assistance.getStatus() != 3) {
                    return ResponseEntity.ok(Map.of("status", "error", "message", "已存在待处理或被同意的助力请求"));
                }
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

            newAssistance.setStatus(0); // 初始状态为 0: 已发布 未接受
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
                    "message", "助力请求创建成功",
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
                return ResponseEntity.ok(Map.of("status", "error", "message", "缺少 assistance_id"));
            }

            // 查找助力信息
            Assistance assistance = assistanceService.getAssistanceById(assistanceId);
            if (assistance == null || !assistance.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "未经授权的行为"));
            }

            // 检查助力状态，只有未被同意的（状态为 0 或 2 或 3）才能修改
            if (assistance.getStatus() != 0 && assistance.getStatus() != 3 && assistance.getStatus() != 2) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "无法修改已被接受的助力请求"));
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
                    "message", "助力信息修改成功",
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
                return ResponseEntity.ok(Map.of("status", "error", "message", "未经授权的行为"));
            }

            // 检查助力状态，只有未被接受的（状态为 0 或 2 或 3 ）才能删除
            if (assistance.getStatus() != 0 && assistance.getStatus() != 2 && assistance.getStatus() != 3) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "无法删除已被接受的助力请求"));
            }

            // 删除助力记录
            assistanceService.deleteAssistanceById(assistanceId);

            return ResponseEntity.ok(Map.of("status", "success", "message", "助力信息删除成功"));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }




    // 搜索助力信息
    @PostMapping("/search")
    public ResponseEntity<?> searchAssistances(@RequestHeader(value = "token", required = false) String headerToken,
                                               @RequestBody Map<String, Object> requestBody) {
        try {
            // 获取 token
            String token = headerToken != null ? headerToken : (String) requestBody.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }

            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 获取搜索关键字
            String keyword = (String) requestBody.get("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请输入搜索关键字"));
            }
            keyword = keyword.trim();

            // 查询用户自己的助力信息
            List<Assistance> assistanceList = assistanceService.getAssistanceByUserId(userId);

            List<Map<String, Object>> responseData = new ArrayList<>();
            for (Assistance assistance : assistanceList) {
                if (assistance.getStatus() == 2 || assistance.getStatus() == 3)
                    continue; // 跳过已拒绝和已取消

                // 检查关键字是否匹配
                boolean match = false;
                Map<String, Object> assistanceData = new HashMap<>();

                // 填充基础数据
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

                // 根据 publicityId 查询对应的 townId
                Integer publicityId = assistance.getPublicityId();
                if (publicityId != null) {
                    Publicity publicity = publicityService.getPublicityById(publicityId);
                    if (publicity != null) {
                        Integer townId = publicity.getTownId();
                        assistanceData.put("town_id", townId);

                        // 根据 townId 查询详细的省市和 townName
                        if (townId != null) {
                            Towns town = townService.getTownById(townId);
                            if (town != null) {
                                assistanceData.put("province", town.getProvince());
                                assistanceData.put("city", town.getCity());
                                assistanceData.put("town_name", town.getTownName());

                                // 检查关键字匹配
                                if (town.getProvince().contains(keyword) ||
                                        town.getCity().contains(keyword) ||
                                        town.getTownName().contains(keyword)) {
                                    match = true;
                                }
                            }
                        }
                    }
                }

                // 检查描述字段是否匹配
                if (assistance.getDescription() != null && assistance.getDescription().contains(keyword)) {
                    match = true;
                }

                // 如果匹配关键字，添加到结果中
                if (match) {
                    responseData.add(assistanceData);
                }
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

}
