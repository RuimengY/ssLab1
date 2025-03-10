package com.example.lab1;

import com.example.lab1.utils.PasswordUtil;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PasswordUtilTest {
    @Test
    void encryptPassword_ReturnsHashedString() {
        String hashed = PasswordUtil.encryptPassword("password123");
        assertThat(hashed).isNotBlank();
        assertThat(hashed).isNotEqualTo("password123");
    }

    @Test
    void checkPassword_ValidPassword_ReturnsTrue() {
        String hashed = PasswordUtil.encryptPassword("password123");
        assertThat(PasswordUtil.checkPassword("password123", hashed)).isTrue();
    }

    @Test
    void checkPassword_InvalidPassword_ReturnsFalse() {
        String hashed = PasswordUtil.encryptPassword("password123");
        assertThat(PasswordUtil.checkPassword("wrongpass", hashed)).isFalse();
    }
}
