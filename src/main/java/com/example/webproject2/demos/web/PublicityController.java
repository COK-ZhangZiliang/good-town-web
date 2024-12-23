package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/publicity")
@Getter
@Setter
@CrossOrigin
public class PublicityController {
    @Autowired
    private TownService townService;

    @Autowired
    private PublicityService publicityService;

    @Autowired
    private AssistanceService assistanceService;

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PublicityRepository publicityRepository;

    // 查询自己所有的宣传信息
    @GetMapping("/my")
    public ResponseEntity<?> getMyPublicity(@RequestHeader("token") String token) {
        try {
            // 解析 token 获取 userId
            if (token == null || token.isEmpty())
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            Integer userId = TokenService.validateToken(token);
            System.out.println("Decoded userId: " + userId);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
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

                    // 解析 image_url 和 video_url 为数组或列表
                    String imageUrls = publicity.getImageUrl(); // 从数据库获取原始 image_url 字段
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        String[] imageUrlArray = imageUrls.split(";");
                        publicityData.put("image_url", imageUrlArray); // 转为数组形式
                    } else {
                        publicityData.put("image_url", new String[0]); // 空数组
                    }

                    String videoUrls = publicity.getVideoUrl(); // 从数据库获取原始 video_url 字段
                    if (videoUrls != null && !videoUrls.isEmpty()) {
                        String[] videoUrlArray = videoUrls.split(";");
                        publicityData.put("video_url", videoUrlArray); // 转为数组形式
                    } else {
                        publicityData.put("video_url", new String[0]); // 空数组
                    }

                    publicityData.put("created_at", publicity.getCreatedAt());
                    publicityData.put("updated_at", publicity.getUpdatedAt());
                    publicityData.put("status", publicity.getStatus());

                    // 获取 townId 并查询 Towns 表
                    Integer townId = publicity.getTownId(); // 假设 Publicity 有 townId 字段
                    if (townId != null) {
                        Optional<Towns> townOptional = townRepository.findById(townId);
                        if (townOptional.isPresent()) {
                            Towns town = townOptional.get();
                            publicityData.put("town_name", town.getTownName());
                            publicityData.put("city", town.getCity());
                            publicityData.put("province", town.getProvince());
                        }
                    }

                    // 获取宣传者的详细信息
                    Optional<Users> userOptional = userRepository.findById(userId);
                    if (userOptional.isPresent()) {
                        Users user = userOptional.get();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", user.getUserId());
                        userData.put("username", user.getUsername());
                        userData.put("phone", user.getPhone());
                        userData.put("bio", user.getBio());
                        userData.put("avatarUrl", user.getAvatarUrl());

                        // 将用户信息添加到宣传信息中
                        publicityData.put("user", userData);
                    }

                    responseData.add(publicityData);
                }
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> addPublicity(@RequestHeader(value = "token", required = false) String headerToken,
                                          @RequestBody Map<String, Object> requestData) {
        try {
            // 获取并验证 token
            String token = headerToken != null ? headerToken : (String) requestData.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }

            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 获取请求参数
            String province = (String) requestData.get("province");
            String city = (String) requestData.get("city");
            String townName = (String) requestData.get("townName");
            String type = (String) requestData.get("type");
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            List<String> imageUrlList = (List<String>) requestData.get("image_url"); // 前端传递的 image_url 数组
            List<String> videoUrlList = (List<String>) requestData.get("video_url"); // 前端传递的 video_url 数组

            // 检查 province, city, townName 参数是否为空
            if (province == null || city == null || townName == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Province, city, and townName are required"));
            }

            // 根据 province, city, townName 查询或创建 TownId
            Integer townId = townService.findOrCreateTown(province, city, townName);
            if (townId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Failed to find or create town"));
            }

            // 检查是否已存在对应的宣传信息
            boolean publicityExists = publicityService.existsByUserIdAndTownId(userId, townId);
            if (publicityExists) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Publicity already exists for this town and user."));
            }

            // 创建宣传信息
            Publicity publicity = new Publicity();
            publicity.setUserId(userId);
            publicity.setTownId(townId);
            publicity.setType(type);
            publicity.setTitle(title);
            publicity.setDescription(description);

            // 将 imageUrlList 和 videoUrlList 转为以 `;` 分隔的字符串
            if (imageUrlList != null && !imageUrlList.isEmpty()) {
                String imageUrl = String.join(";", imageUrlList); // 使用 ; 连接
                publicity.setImageUrl(imageUrl);
            } else {
                publicity.setImageUrl(null); // 如果为空，保存为 null
            }

            if (videoUrlList != null && !videoUrlList.isEmpty()) {
                String videoUrl = String.join(";", videoUrlList); // 使用 ; 连接
                publicity.setVideoUrl(videoUrl);
            } else {
                publicity.setVideoUrl(null); // 如果为空，保存为 null
            }

            publicity.setStatus(0); // 默认状态为已发布

            // 设置时间字段
            LocalDateTime now = LocalDateTime.now();
            publicity.setCreatedAt(now);
            publicity.setUpdatedAt(now);

            publicity = publicityService.addPublicity(publicity);

            // 构建返回数据
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
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }




    // 查询用户发布的宣传信息下的助力请求
    @GetMapping("/assistance/requests")
    public ResponseEntity<?> getPublicityAssistanceRequests(@RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }
            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 查询用户发布的所有宣传信息
            List<Publicity> publicityList = publicityService.getReleasedPublicity(userId);
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

                // 解析 image_url 和 video_url 为数组或列表
                String imageUrls = publicity.getImageUrl(); // 从数据库获取原始 image_url 字段
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String[] imageUrlArray = imageUrls.split(";");
                    publicityData.put("image_url", imageUrlArray); // 转为数组形式
                } else {
                    publicityData.put("image_url", new String[0]); // 空数组
                }

                String videoUrls = publicity.getVideoUrl(); // 从数据库获取原始 video_url 字段
                if (videoUrls != null && !videoUrls.isEmpty()) {
                    String[] videoUrlArray = videoUrls.split(";");
                    publicityData.put("video_url", videoUrlArray); // 转为数组形式
                } else {
                    publicityData.put("video_url", new String[0]); // 空数组
                }

                publicityData.put("created_at", publicity.getCreatedAt());
                publicityData.put("updated_at", publicity.getUpdatedAt());

                // 获取 townId 并查询 Towns 表
                Integer townId = publicity.getTownId(); // 假设 Publicity 有 townId 字段
                if (townId != null) {
                    Optional<Towns> townOptional = townRepository.findById(townId);
                    if (townOptional.isPresent()) {
                        Towns town = townOptional.get();
                        publicityData.put("town_name", town.getTownName());
                        publicityData.put("city", town.getCity());
                        publicityData.put("province", town.getProvince());
                    }
                }

                // 获取宣传者的详细信息
                Optional<Users> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    Users user = userOptional.get();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", user.getUserId());
                    userData.put("username", user.getUsername());
                    userData.put("phone", user.getPhone());
                    userData.put("bio", user.getBio());
                    userData.put("avatarUrl", user.getAvatarUrl());

                    // 将用户信息添加到宣传信息中
                    publicityData.put("user", userData);
                }

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
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    // 修改宣传信息
    // 修改宣传信息
    @PutMapping("/update/{publicityId}")
    public ResponseEntity<?> updatePublicity(@RequestHeader("token") String token,
                                             @PathVariable Integer publicityId,
                                             @RequestBody Map<String, Object> requestData) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }
            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 获取要修改的宣传信息
            Publicity publicity = publicityService.getPublicityById(publicityId);
            if (publicity == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Publicity not found"));
            }

            // 确保用户只能修改自己发布的宣传信息
            if (!publicity.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "You do not have permission to edit this publicity"));
            }

            Integer status = (Integer) requestData.get("status");

            // 需要验证是否有待处理的助力请求
            List<Assistance> assistanceRequests = assistanceService.getAssistanceByPublicityId(publicityId);
            for (Assistance assistance : assistanceRequests) {
                if (assistance.getStatus() == 0) { // 如果存在待接受的助力请求
                    return ResponseEntity.ok(Map.of("status", "error", "message", "Cannot publish publicity with active assistance requests"));
                }
            }

            // 更新宣传信息
            if (requestData.get("title") != null) {
                publicity.setTitle((String) requestData.get("title"));
            }
            if (requestData.get("description") != null) {
                publicity.setDescription((String) requestData.get("description"));
            }
            if (requestData.get("image_url") != null) {
                Object imageUrlObject = requestData.get("image_url");
                if (imageUrlObject instanceof List) { // 检查是否为数组
                    List<String> imageUrlList = (List<String>) imageUrlObject;
                    String imageUrl = String.join(";", imageUrlList); // 将数组转换为以 `;` 分隔的字符串
                    publicity.setImageUrl(imageUrl);
                } else if (imageUrlObject instanceof String) { // 单个字符串
                    publicity.setImageUrl((String) imageUrlObject);
                }
            }
            if (requestData.get("video_url") != null) {
                Object videoUrlObject = requestData.get("video_url");
                if (videoUrlObject instanceof List) { // 检查是否为数组
                    List<String> videoUrlList = (List<String>) videoUrlObject;
                    String videoUrl = String.join(";", videoUrlList); // 将数组转换为以 `;` 分隔的字符串
                    publicity.setVideoUrl(videoUrl);
                } else if (videoUrlObject instanceof String) { // 单个字符串
                    publicity.setVideoUrl((String) videoUrlObject);
                }
            }
            if (status != null) {
                publicity.setStatus(status);
            }

            // 更新时间
            publicity.setUpdatedAt(LocalDateTime.now());

            // 保存修改后的宣传信息
            publicityService.addPublicity(publicity);

            // 返回修改后的数据
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

            return ResponseEntity.ok(Map.of("status", "success", "message", "Publicity updated successfully!", "data", publicityData));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }



    // 删除宣传信息
    @DeleteMapping("/delete/{publicityId}")
    public ResponseEntity<?> deletePublicity(@RequestHeader("token") String token,
                                             @PathVariable Integer publicityId) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "请登录"));
            }
            // 验证 token 并获取 userId
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "登录过期，请重新登录"));
            }

            // 查询宣传信息
            Publicity publicity = publicityService.getPublicityById(publicityId);
            if (publicity == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Publicity not found"));
            }

            // 验证该宣传信息是否属于当前用户
            if (!publicity.getUserId().equals(userId)) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "You do not have permission to delete this publicity"));
            }

            // 检查该宣传信息是否有待处理的助力请求
            List<Assistance> assistanceList = assistanceService.getAssistanceByPublicityId(publicityId);
            for (Assistance assistance : assistanceList) {
                if (assistance.getStatus() == 0 || assistance.getStatus() == 1 || assistance.getStatus() == 2) {
                    return ResponseEntity.ok(Map.of("status", "error", "message", "Cannot delete publicity with active assistance requests"));
                }
            }

            // 删除宣传信息
            publicityRepository.delete(publicity);

            return ResponseEntity.ok(Map.of("status", "success", "message", "Publicity deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllPublicity() {
        try {
            // 查询所有状态为已发布（status = 0）的宣传信息
            List<Publicity> publicityList = publicityService.getPublicityByStatus(0);

            // 宣传信息构造响应数据
            List<Map<String, Object>> responseData = new ArrayList<>();
            for (Publicity publicity : publicityList) {
                Map<String, Object> publicityData = new HashMap<>();
                publicityData.put("publicity_id", publicity.getPublicityId());
                publicityData.put("town_id", publicity.getTownId());
                publicityData.put("title", publicity.getTitle());
                publicityData.put("type", publicity.getType());
                publicityData.put("description", publicity.getDescription());

                // 解析 image_url 和 video_url 为数组或列表
                String imageUrls = publicity.getImageUrl(); // 从数据库获取原始 image_url 字段
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String[] imageUrlArray = imageUrls.split(";");
                    publicityData.put("image_url", imageUrlArray); // 转为数组形式
                } else {
                    publicityData.put("image_url", new String[0]); // 空数组
                }

                String videoUrls = publicity.getVideoUrl(); // 从数据库获取原始 video_url 字段
                if (videoUrls != null && !videoUrls.isEmpty()) {
                    String[] videoUrlArray = videoUrls.split(";");
                    publicityData.put("video_url", videoUrlArray); // 转为数组形式
                } else {
                    publicityData.put("video_url", new String[0]); // 空数组
                }

                publicityData.put("created_at", publicity.getCreatedAt());
                publicityData.put("updated_at", publicity.getUpdatedAt());

                // 获取 townId 并查询 Towns 表
                Integer townId = publicity.getTownId(); // 假设 Publicity 有 townId 字段
                if (townId != null) {
                    Optional<Towns> townOptional = townRepository.findById(townId);
                    if (townOptional.isPresent()) {
                        Towns town = townOptional.get();
                        publicityData.put("town_name", town.getTownName());
                        publicityData.put("city", town.getCity());
                        publicityData.put("province", town.getProvince());
                    }
                }

                // 获取宣传者的详细信息
                Integer userId = publicity.getUserId(); // 假设 Publicity 有 userId 字段
                if (userId != null) {
                    Optional<Users> userOptional = userRepository.findById(userId);
                    if (userOptional.isPresent()) {
                        Users user = userOptional.get();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", user.getUserId());
                        userData.put("username", user.getUsername());
                        userData.put("phone", user.getPhone());
                        userData.put("bio", user.getBio());
                        userData.put("avatarUrl", user.getAvatarUrl());

                        // 将用户信息添加到宣传信息中
                        publicityData.put("user", userData);
                    }
                }

                // 查询该宣传信息下的助力请求
                List<Assistance> assistanceRequests = assistanceService.getAssistanceByPublicityId(publicity.getPublicityId());

                // 构造助力请求数据
                List<Map<String, Object>> assistanceDataList = new ArrayList<>();
                for (Assistance assistance : assistanceRequests) {
                    if (assistance.getStatus() == 1) // 只返回已接受的助力请求
                    {

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
                }

                publicityData.put("assistance_requests", assistanceDataList);


                responseData.add(publicityData);
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }




    // 查询宣传的详细信息
    @GetMapping("/{publicityId}")
    public ResponseEntity<?> getPublicityDetails(@PathVariable Integer publicityId) {
        try {
            Publicity publicity = publicityService.getPublicityById(publicityId);
            if (publicity == null) {
                return ResponseEntity.ok(Map.of("status", "error", "message", "Publicity not found"));
            }

            Map<String, Object> publicityData = new HashMap<>();
            publicityData.put("publicity_id", publicity.getPublicityId());
            publicityData.put("town_id", publicity.getTownId());
            publicityData.put("title", publicity.getTitle());
            publicityData.put("type", publicity.getType());
            publicityData.put("description", publicity.getDescription());

            // 解析 image_url 和 video_url 为数组或列表
            String imageUrls = publicity.getImageUrl(); // 从数据库获取原始 image_url 字段
            if (imageUrls != null && !imageUrls.isEmpty()) {
                String[] imageUrlArray = imageUrls.split(";");
                publicityData.put("image_url", imageUrlArray); // 转为数组形式
            } else {
                publicityData.put("image_url", new String[0]); // 空数组
            }

            String videoUrls = publicity.getVideoUrl(); // 从数据库获取原始 video_url 字段
            if (videoUrls != null && !videoUrls.isEmpty()) {
                String[] videoUrlArray = videoUrls.split(";");
                publicityData.put("video_url", videoUrlArray); // 转为数组形式
            } else {
                publicityData.put("video_url", new String[0]); // 空数组
            }

            publicityData.put("created_at", publicity.getCreatedAt());
            publicityData.put("updated_at", publicity.getUpdatedAt());


            // 获取 townId 并查询 Towns 表
            Integer townId = publicity.getTownId(); // 假设 Publicity 有 townId 字段
            if (townId != null) {
                Optional<Towns> townOptional = townRepository.findById(townId);
                if (townOptional.isPresent()) {
                    Towns town = townOptional.get();
                    publicityData.put("town_name", town.getTownName());
                    publicityData.put("city", town.getCity());
                    publicityData.put("province", town.getProvince());
                }
            }

            return ResponseEntity.ok(Map.of("status", "success", "data", publicityData));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }


}