package com.example.webproject2.demos.web;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    // 上传文件（包括图片和视频）
    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("status", "error");
            result.put("message", "上传失败，文件为空");
            return result;
        }
        try {
            // 确保上传目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            String fileName = file.getOriginalFilename();
            String filePath = uploadDir + "/" + fileName;
            file.transferTo(new File(filePath));

            // 判断文件类型（图片或视频）
            String fileType;
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image")) {
                fileType = "image";
            } else if (contentType != null && contentType.startsWith("video")) {
                fileType = "video";
            } else {
                fileType = "unknown";
            }

            // 返回结果
            result.put("status", "success");
            result.put("message", "上传成功");
            result.put("file_type", fileType); // 文件类型
            result.put("file_name", fileName); // 文件名
            result.put("file_url", accessUrl + fileName); // 可访问的 URL
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "上传失败：" + e.getMessage());
            return result;
        }
    }
}

