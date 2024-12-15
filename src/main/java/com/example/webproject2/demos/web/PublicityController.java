package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/publicity")
@Getter
@Setter
public class PublicityController {

    @Autowired
    private PublicityService publicityService;

    @Autowired
    private AssistanceService assistanceService;

    @Autowired
    private UserRepository userRepository;

    // 查询自己所有的宣传信息
    @GetMapping("/my")
    public ResponseEntity<?> getMyPublicity(@RequestHeader("token") String token) {
        try {
            // 解析 token 获取 userId
            Integer userId = TokenService.validateToken(token);
            System.out.println("Decoded userId: " + userId);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "error", "message", "Invalid token"));
            }

            // 获取用户的宣传信息
            List<Publicity> publicityList = null;
            try {
                publicityList = publicityService.getMyPublicity(userId);
                if (publicityList == null || publicityList.isEmpty()) {
                    System.out.println("Fetched publicityList is empty or null.");
                } else {
                    System.out.println("Fetched publicityList: " + publicityList);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error: " + e.getMessage());
            }

            // 构造响应数据
            List<Map<String, Object>> responseData = new ArrayList<>();
            if (publicityList != null) {
                for (Publicity publicity : publicityList) {
                    Map<String, Object> publicityData = new HashMap<>();
                    publicityData.put("publicity_id", publicity.getPublicityId());
                    publicityData.put("town_id", publicity.getTownId());
                    publicityData.put("type", publicity.getType());
                    publicityData.put("title", publicity.getTitle());
                    publicityData.put("description", publicity.getDescription());
                    publicityData.put("image_url", publicity.getImageUrl());
                    publicityData.put("video_url", publicity.getVideoUrl());
                    publicityData.put("created_at", publicity.getCreatedAt());
                    publicityData.put("updated_at", publicity.getUpdatedAt());
                    publicityData.put("status", publicity.getStatus());
                    responseData.add(publicityData);
                }
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // 创建宣传信息
    @PostMapping("/create")
    public ResponseEntity<?> addPublicity(@RequestHeader(value = "token", required = false) String headerToken,
                                          @RequestBody Map<String, Object> requestData) {
        try {
            String token = headerToken != null ? headerToken : (String) requestData.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Token is required"));
            }

            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "error", "message", "Invalid token"));
            }

            Integer townId = (Integer) requestData.get("town_id");
            String type = (String) requestData.get("type");
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            String imageUrl = (String) requestData.get("image_url");
            String videoUrl = (String) requestData.get("video_url");

            boolean publicityExists = publicityService.existsByUserIdAndTownId(userId, townId);
            if (publicityExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "error", "message", "Publicity already exists for this town and user."));
            }

            Publicity publicity = new Publicity();
            publicity.setUserId(userId);
            publicity.setTownId(townId);
            publicity.setType(type);
            publicity.setTitle(title);
            publicity.setDescription(description);
            publicity.setImageUrl(imageUrl);
            publicity.setVideoUrl(videoUrl);
            publicity.setStatus(0);

            // 设置时间字段
            LocalDateTime now = LocalDateTime.now();
            publicity.setCreatedAt(now);
            publicity.setUpdatedAt(now);

            publicity = publicityService.addPublicity(publicity);

            Map<String, Object> publicityData = new HashMap<>();
            publicityData.put("publicity_id", publicity.getPublicityId());
            publicityData.put("user_id", publicity.getUserId());
            publicityData.put("town_id", publicity.getTownId());
            publicityData.put("type", publicity.getType());
            publicityData.put("title", publicity.getTitle());
            publicityData.put("description", publicity.getDescription());
            publicityData.put("image_url", publicity.getImageUrl());
            publicityData.put("video_url", publicity.getVideoUrl());
            publicityData.put("status", publicity.getStatus());
            publicityData.put("created_at", publicity.getCreatedAt());
            publicityData.put("updated_at", publicity.getUpdatedAt());

            return ResponseEntity.ok(Map.of("status", "success", "message", "Publicity added successfully!", "data", publicityData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }



    // 查询用户发布的宣传信息下的助力请求
    @GetMapping("/assistance/requests")
    public ResponseEntity<?> getPublicityAssistanceRequests(@RequestHeader("token") String token) {
        try {
            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "error", "message", "Invalid token"));
            }

            // 查询用户发布的所有宣传信息
            List<Publicity> publicityList = publicityService.getMyPublicity(userId);
            if (publicityList == null || publicityList.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "success", "data", Collections.emptyList()));
            }

            // 构造响应数据
            List<Map<String, Object>> responseData = new ArrayList<>();

            for (Publicity publicity : publicityList) {
                Map<String, Object> publicityData = new HashMap<>();
                publicityData.put("publicity_id", publicity.getPublicityId());
                publicityData.put("town_id", publicity.getTownId());
                publicityData.put("title", publicity.getTitle());
                publicityData.put("type", publicity.getType());
                publicityData.put("description", publicity.getDescription());

                // 查询该宣传信息下的助力请求
                List<Assistance> assistanceRequests = assistanceService.getAssistanceByPublicityId(publicity.getPublicityId());

                // 构造助力请求数据
                List<Map<String, Object>> assistanceDataList = new ArrayList<>();
                for (Assistance assistance : assistanceRequests) {
                    Map<String, Object> assistanceData = new HashMap<>();
                    assistanceData.put("assistance_id", assistance.getAssistanceId());
                    assistanceData.put("user_id", assistance.getUserId());
                    assistanceData.put("description", assistance.getDescription());
                    assistanceData.put("image_url", assistance.getImageUrl());
                    assistanceData.put("video_url", assistance.getVideoUrl());
                    assistanceData.put("status", assistance.getStatus());

                    // 查询请求用户的信息
                    userRepository.findById(assistance.getUserId()).ifPresent(user -> {
                        assistanceData.put("user_name", user.getName());
                    });

                    assistanceDataList.add(assistanceData);
                }

                publicityData.put("assistance_requests", assistanceDataList);
                responseData.add(publicityData);
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    // 修改宣传信息
//    @PutMapping("/update/{publicityId}")
}