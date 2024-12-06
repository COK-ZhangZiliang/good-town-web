package com.example.webproject2.demos.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String token;
    private String idType;
    private String idNumber;
    private String phone;
    private String bio;
    private String password;

}