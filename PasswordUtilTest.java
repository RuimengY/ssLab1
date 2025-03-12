package com.example.lab1;

import com.example.lab1.utils.PasswordUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    void encryptPassword_ValidPassword_ReturnsEncryptedPassword() {
        // 执行测试
        String plainPassword = "Test123";
        String encryptedPassword = PasswordUtil.encryptPassword(plainPassword);

        // 验证结果
        assertNotNull(encryptedPassword);
        assertNotEquals(plainPassword, encryptedPassword);
        assertTrue(encryptedPassword.startsWith("$2a$"));
    }

    @Test
    void checkPassword_CorrectPassword_ReturnsTrue() {
        // 准备测试数据
        String plainPassword = "Test123";
        String encryptedPassword = PasswordUtil.encryptPassword(plainPassword);

        // 执行测试
        boolean result = PasswordUtil.checkPassword(plainPassword, encryptedPassword);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void checkPassword_IncorrectPassword_ReturnsFalse() {
        // 准备测试数据
        String correctPassword = "Test123";
        String wrongPassword = "Test456";
        String encryptedPassword = PasswordUtil.encryptPassword(correctPassword);

        // 执行测试
        boolean result = PasswordUtil.checkPassword(wrongPassword, encryptedPassword);

        // 验证结果
        assertFalse(result);
    }
}

