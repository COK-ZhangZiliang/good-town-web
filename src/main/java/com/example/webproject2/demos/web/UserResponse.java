package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String status;
    private String message;
    private Object data; // 包含用户信息或错误信息
    private List<String> errorMessages;

    // 构造函数（用于成功响应）
    public UserResponse(String status, String message, UserData userData) {
        this.status = status;
        this.message = message;
        this.data = userData;
    }

    // 构造函数（用于错误响应）
    public UserResponse(String status, String message, List<String> errorMessages) {
        this.status = status;
        this.message = message;
        this.errorMessages = errorMessages;
    }

    // 构造函数（用于带错误码的错误响应）
    public UserResponse(String status, String message, String errorCode, int statusCode) {
        this.status = status;
        this.message = message;
        this.data = new ErrorResponse(errorCode, statusCode);
    }


    // 内部类用于存储成功返回的用户数据
    @Getter
    @Setter
    public static class UserData {
        private Integer userId;
        private String username;
        private String name;
        private String idType;
        private String idNumber;
        private String phone;
        private String bio;
        private String token; // 仅在登录成功时使用
        private Byte userType;
        private String avatarUrl;

        public UserData(Integer userId, String username, String name, String idType, String idNumber, String phone, String bio, String avatarUrl, Byte userType) {
            this.userId = userId;
            this.username = username;
            this.name = name;
            this.idType = idType;
            this.idNumber = idNumber;
            this.phone = phone;
            this.bio = bio;
            this.userType = userType;
            this.avatarUrl = avatarUrl;
        }

        public UserData(Integer userId, String username, String phone, String bio, String avatarUrl) {
            this.userId = userId;
            this.username = username;
            this.phone = phone;
            this.bio = bio;
            this.avatarUrl = avatarUrl;
        }
    }



    // 内部类用于存储错误信息
    @Getter
    @Setter
    public static class ErrorResponse {
        private String errorCode;
        private int statusCode;

        public ErrorResponse(String errorCode, int statusCode) {
            this.errorCode = errorCode;
            this.statusCode = statusCode;
        }
    }

    // 设置 token 到 data（如果 data 是 UserData 类型）
    public void setTokenForData(String token) {
        if (this.data instanceof UserData) {
            ((UserData) this.data).setToken(token);
        } else {
            throw new IllegalStateException("data is not of type UserData");
        }
    }
}
