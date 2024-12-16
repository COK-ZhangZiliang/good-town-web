package com.example.webproject2.demos.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class TokenService {

    private static final String SECRET_KEY = "mySecretKey";  // 简化起见使用硬编码的密钥

    public static String generateToken(int userId, String username) {
        String dynamicSecretKey = SECRET_KEY + userId + username;  // 使用 userId 和 username 来动态生成密钥
        Algorithm algorithm = Algorithm.HMAC256(dynamicSecretKey); // 动态密钥
//        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY); // 使用 HMAC256 算法
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000 * 3)) // 3小时过期
                .sign(algorithm);
    }

    // 校验 token 是否有效
    public static Integer validateToken(String token) {
        try {
            // 解码并验证 token
            DecodedJWT decodedJWT = JWT.decode(token);
            String username = decodedJWT.getSubject();
            Integer userId = decodedJWT.getClaim("userId").asInt();

            // 使用动态密钥来验证 token
            String dynamicSecretKey = SECRET_KEY + userId + username;  // 动态密钥
            Algorithm algorithm = Algorithm.HMAC256(dynamicSecretKey); // 使用 HMAC256 算法

            // 创建 JWT 验证器
            JWTVerifier verifier = JWT.require(algorithm)
                    .withSubject(username)
                    .build();

            // 验证 token 是否有效
            verifier.verify(token);

            // 返回验证通过的用户 ID
            return userId;

        } catch (JWTVerificationException exception) {
            // token 校验失败
            System.out.println("Token验证失败: " + exception.getMessage());
            return null;
        }
    }
}