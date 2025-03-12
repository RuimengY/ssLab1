package com.example.lab1;

import com.example.lab1.utils.CaptchaUtil;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class CaptchaUtilTest {

    @Test
    void generateCaptchaText_ReturnsTextWith6Digits() {
        // 执行测试
        String captchaText = CaptchaUtil.generateCaptchaText();

        // 验证结果
        assertNotNull(captchaText);
        assertEquals(6, captchaText.length());
        assertTrue(captchaText.matches("\\d{6}"));
    }

    @Test
    void generateCaptcha_ReturnsCaptchaTextAndImage() {
        // 执行测试
        Map<String, Object> result = CaptchaUtil.generateCaptcha();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("text"));
        assertTrue(result.containsKey("image"));

        String text = (String) result.get("text");
        BufferedImage image = (BufferedImage) result.get("image");

        assertNotNull(text);
        assertNotNull(image);
        assertEquals(6, text.length());
        assertEquals(120, image.getWidth());
        assertEquals(40, image.getHeight());
    }

    @Test
    void storeCaptcha_ReturnsCaptchaId() {
        // 执行测试
        String captchaText = "123456";
        String captchaId = CaptchaUtil.storeCaptcha(captchaText);

        // 验证结果
        assertNotNull(captchaId);
        assertTrue(captchaId.length() > 0);
    }

    @Test
    void validateCaptcha_CorrectInput_ReturnsTrue() {
        // 准备测试数据
        String captchaText = "123456";
        String captchaId = CaptchaUtil.storeCaptcha(captchaText);

        // 执行测试
        boolean result = CaptchaUtil.validateCaptcha(captchaId, captchaText);

        // 验证结果
        assertTrue(result);

        // 验证一次性使用
        boolean secondAttempt = CaptchaUtil.validateCaptcha(captchaId, captchaText);
        assertFalse(secondAttempt);
    }

    @Test
    void validateCaptcha_IncorrectInput_ReturnsFalse() {
        // 准备测试数据
        String captchaText = "123456";
        String wrongInput = "654321";
        String captchaId = CaptchaUtil.storeCaptcha(captchaText);

        // 执行测试
        boolean result = CaptchaUtil.validateCaptcha(captchaId, wrongInput);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void validateCaptcha_InvalidCaptchaId_ReturnsFalse() {
        // 执行测试
        boolean result = CaptchaUtil.validateCaptcha("invalid-id", "123456");

        // 验证结果
        assertFalse(result);
    }
}
