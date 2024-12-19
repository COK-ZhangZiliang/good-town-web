package com.example.webproject2.demos.web;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@RestController
@Transactional
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // 用户注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest, BindingResult result) {
        // 如果请求体中的数据校验失败，返回错误信息
        if (result.hasErrors()) {
            // 创建一个错误响应对象来存储错误信息
            List<String> errorMessages = new ArrayList<>();

            // 遍历所有验证错误并将错误消息添加到 errorMessages 列表
            for (FieldError fieldError : result.getFieldErrors()) {
                String errorMessage = String.format("Field '%s' %s",
                        fieldError.getField(),
                        fieldError.getDefaultMessage());
                errorMessages.add(errorMessage);
            }

            // 打印所有验证错误
            System.out.println("Validation errors: " + errorMessages);

            // 创建统一的错误响应
            UserResponse errorResponse = new UserResponse("error", "验证失败", errorMessages);

            // 返回包含验证错误的响应体
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 检查用户名是否已存在
        Optional<Users> existingUser = userRepository.findByUsername(userRequest.getUsername());
        if (existingUser.isPresent()) {
            // 打印日志信息
            System.out.println("用户名已存在");

            // 用户名已存在
            UserResponse errorResponse = new UserResponse("error", "用户名已存在", "USER_ALREADY_EXISTS", 400);
            return ResponseEntity.ok(errorResponse);
        }

        // 创建新用户
        Users newUser = new Users();
        newUser.setUsername(userRequest.getUsername());
        newUser.setPassword(userRequest.getPassword());
        newUser.setName(userRequest.getName());
        newUser.setIdType(userRequest.getIdType());
        newUser.setIdNumber(userRequest.getIdNumber());
        newUser.setPhone(userRequest.getPhone());
        newUser.setBio(userRequest.getBio());

        // 保存用户信息
        Users savedUser = userRepository.save(newUser);

        // 创建一个包含详细用户信息的 UserResponse
        UserResponse successResponse = new UserResponse(
                "success",
                "用户注册成功",
                new UserResponse.UserData(
                        savedUser.getUserId(),
                        savedUser.getUsername(),
                        savedUser.getName(),
                        savedUser.getIdType(),
                        savedUser.getIdNumber(),
                        savedUser.getPhone(),
                        savedUser.getBio()
                )
        );

        // 返回包含用户详细信息的成功响应
        return ResponseEntity.ok(successResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest userRequest) {
        // 检查用户名是否存在
        Optional<Users> existingUser = userRepository.findByUsername(userRequest.getUsername());
        if (existingUser.isEmpty()) {
            // 用户名不存在
            System.out.println("用户名不存在");
            UserResponse errorResponse = new UserResponse("error", "用户名错误", (UserResponse.UserData) null);
            return ResponseEntity.ok(errorResponse);
        }

        Users user = existingUser.get();

        // 检查密码是否正确 (简化起见，不考虑密码加密)
        if (!user.getPassword().equals(userRequest.getPassword())) {
            // 密码错误
            System.out.println("密码错误");
            UserResponse errorResponse = new UserResponse("error", "密码错误", (UserResponse.UserData) null);
            return ResponseEntity.ok(errorResponse);
        }

        System.out.println("登录成功");

        // 生成JWT token
        String token = TokenService.generateToken(user.getUserId(), user.getUsername());

        // 创建成功的 UserResponse 对象
        UserResponse successResponse = new UserResponse(
                "success",
                "登录成功",
                (UserResponse.UserData) null
        );

        // 创建 data 对象，包含用户信息和 token
        UserResponse.UserData userData = new UserResponse.UserData(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getIdType(),
                user.getIdNumber(),
                user.getPhone(),
                user.getBio()
        );

        // 设置 successResponse 的 data
        successResponse.setData(userData);

        // 将 token 添加到响应数据中
        successResponse.setMessage("登录成功");
        // 设置 token 到 data 中
        successResponse.setTokenForData(token);

        return ResponseEntity.ok(successResponse);
    }


    // 修改个人信息接口
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader(value = "token", required = false) String headerToken,
            @RequestBody UserUpdateRequest updateRequest) {

        // 优先从请求头中获取 token
        String token = headerToken;

        // 如果头部中没有 token，再从请求体中获取
        if ((token == null || token.isEmpty()) && updateRequest.getToken() != null) {
            token = updateRequest.getToken();
        }

        // 如果 token 仍然为空，返回错误
        if (token == null || token.isEmpty()) {
            System.out.println("认证失败或请求参数错误");
            UserResponse errorResponse = new UserResponse("error", "认证失败或请求参数错误", (UserResponse.UserData) null);
            return ResponseEntity.status(400).body(errorResponse);
        }

        // 验证 token
        Integer userId = TokenService.validateToken(token);
        if (userId == null) {
            System.out.println("认证失败，token无效");
            UserResponse errorResponse = new UserResponse("error", "认证失败，token无效", (UserResponse.UserData) null);
            return ResponseEntity.status(401).body(errorResponse);
        }

        // 获取用户信息
        Optional<Users> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            System.out.println("用户不存在");
            UserResponse errorResponse = new UserResponse("error", "用户不存在", (UserResponse.UserData) null);
            return ResponseEntity.ok(errorResponse);
        }

        Users user = userOptional.get();

        // 更新用户信息的逻辑（与之前一致）
        if (updateRequest.getIdType() != null) {
            System.out.println("ID Type: " + updateRequest.getIdType());
            user.setIdType(updateRequest.getIdType());
        }
        if (updateRequest.getIdNumber() != null) {
            System.out.println("ID Number: " + updateRequest.getIdNumber());
            user.setIdNumber(updateRequest.getIdNumber());
        }
        if (updateRequest.getPhone() != null) {
            System.out.println("Phone: " + updateRequest.getPhone());
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getBio() != null) {
            System.out.println("Bio: " + updateRequest.getBio());
            user.setBio(updateRequest.getBio());
        }
        if (updateRequest.getPassword() != null && updateRequest.getPassword().length() >= 6 && updateRequest.getPassword().matches(".*\\d.*")) {
            System.out.println("Password: " + updateRequest.getPassword());
            user.setPassword(updateRequest.getPassword());
        } else if (updateRequest.getPassword() != null) {
            System.out.println("密码至少6位并包含至少2个数字");
            UserResponse errorResponse = new UserResponse("error", "密码至少6位并包含至少2个数字", (UserResponse.UserData) null);
            return ResponseEntity.ok(errorResponse);
        }

        // 保存更新后的用户信息
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        entityManager.flush(); // 强制将更改刷新到数据库
        entityManager.refresh(user); // 刷新实体以获取最新的数据

        System.out.println("用户信息更新成功");

        // 创建返回数据的 UserData 对象
        UserResponse.UserData userData = new UserResponse.UserData(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getIdType(),
                user.getIdNumber(),
                user.getPhone(),
                user.getBio()
        );

        // 返回成功响应，带有更新后的用户信息
        return ResponseEntity.ok(new UserResponse("success", "用户信息更新成功", userData));
    }
}