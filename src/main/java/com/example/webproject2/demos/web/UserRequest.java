package com.example.webproject2.demos.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度应在3到50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    @NotBlank(message = "用户姓名不能为空")
    private String name;

    private String idType;

    private String idNumber;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String bio;
}