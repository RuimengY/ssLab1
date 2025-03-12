package com.example.lab1;

import com.example.lab1.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JWTUtilTest {

    private static final String TEST_SECRET_KEY = "test-secret-key-with-at-least-32-characters";
    private static final String TEST_USERNAME = "testUser";

    @Test
    void generateToken_ValidUsername_ReturnsToken() {
        // 设置测试密钥
        JWTUtil.setSecretKey(TEST_SECRET_KEY);

        // 执行测试
        String token = JWTUtil.generateToken(TEST_USERNAME);

        // 验证结果
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // 验证token内容
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(TEST_USERNAME, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        // 设置测试密钥
        JWTUtil.setSecretKey(TEST_SECRET_KEY);

        // 创建一个有效的token
        String token = JWTUtil.generateToken(TEST_USERNAME);

        // 执行测试
        String username = JWTUtil.getUsernameFromToken(token);

        // 验证结果
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // 设置测试密钥
        JWTUtil.setSecretKey(TEST_SECRET_KEY);

        // 创建一个有效的token
        String token = JWTUtil.generateToken(TEST_USERNAME);

        // 执行测试
        boolean valid = JWTUtil.validateToken(token);

        // 验证结果
        assertTrue(valid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // 设置测试密钥
        JWTUtil.setSecretKey(TEST_SECRET_KEY);

        // 一个无效的token
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTYxNjE2Njc5MywiZXhwIjoxNjE2MjUzMTkzfQ.invalid-signature";

        // 执行测试
        boolean valid = JWTUtil.validateToken(invalidToken);

        // 验证结果
        assertFalse(valid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // 设置测试密钥和过期时间
        JWTUtil.setSecretKey(TEST_SECRET_KEY);
        long originalExpiration = JWTUtil.getExpirationTime();
        JWTUtil.setExpirationTime(-10000L); // 过期的token

        try {
            // 创建一个已过期的token
            String expiredToken = JWTUtil.generateToken(TEST_USERNAME);

            // 执行测试
            boolean valid = JWTUtil.validateToken(expiredToken);

            // 验证结果
            assertFalse(valid);
        } finally {
            // 恢复原始过期时间
            JWTUtil.setExpirationTime(originalExpiration);
        }
    }
}