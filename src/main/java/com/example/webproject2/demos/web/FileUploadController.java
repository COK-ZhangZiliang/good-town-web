package com.example.webproject2.demos.web;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    // 上传文件（包括图片和视频）
    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestHeader(value = "token", required = false) String headerToken,
                                          @RequestParam(value = "token", required = false) String paramToken,
                                          @RequestParam("file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        try {
            // 获取并验证 token：首先检查请求头中的 token，其次检查请求参数中的 token
            String token = headerToken != null ? headerToken : paramToken;

            if (token == null || token.isEmpty()) {
                result.put("status", "error");
                result.put("message", "请登录");
                return result;
            }

            // 验证 token 的有效性
            Integer userId = TokenService.validateToken(token);
            if (userId == null) {
                result.put("status", "error");
                result.put("message", "登录过期，请重新登录");
                return result;
            }

            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("status", "error");
                result.put("message", "上传失败，文件为空");
                return result;
            }

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
            // 捕获文件上传中的 IO 异常
            e.printStackTrace(); // 打印异常堆栈，方便调试
            result.put("status", "error");
            result.put("message", "上传失败：文件操作异常");
            return result;

        } catch (Exception e) {
            // 捕获所有其他异常
            e.printStackTrace(); // 打印异常堆栈，方便调试
            result.put("status", "error");
            result.put("message", "上传失败：" + e.getMessage());
            return result;
        }
    }


    // 根据 URL 下载文件
    @GetMapping("/download")
    public void downloadFile(@RequestParam("url") String fileUrl, HttpServletResponse response) {
        try {
            // 解析文件名
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }

            // 确定文件的存储路径
            File file = new File(uploadDir + "/" + fileName);

            // 检查文件是否存在
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }

            // 对文件名进行 URL 编码，避免中文或特殊字符导致问题
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLength((int) file.length());

            // 写入文件内容到响应
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件下载失败：" + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }



}

