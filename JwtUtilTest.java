package com.example.lab1;

import com.example.lab1.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {
    @Test
    void generateToken_ValidUsername_ReturnsToken() {
        String token = JwtUtil.generateToken("testuser");
        assertThat(token).isNotBlank();
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        String token = JwtUtil.generateToken("testuser");
        String username = JwtUtil.getUsernameFromToken(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = JwtUtil.generateToken("testuser");
        assertThat(JwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertThat(JwtUtil.validateToken("invalid.token.here")).isFalse();
    }
}
