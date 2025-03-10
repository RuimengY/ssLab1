package com.example.lab1;

import org.junit.jupiter.api.Test;
import com.example.lab1.utils.CaptchaUtil;
import static org.assertj.core.api.Assertions.assertThat;

class CaptchaUtilTest {
    @Test
    void generateCaptcha_Returns4Digits() {
        String captcha = CaptchaUtil.generateCaptcha();
        assertThat(captcha).hasSize(4);
        assertThat(captcha).matches("\\d{4}");
    }

    @Test
    void verifyCaptcha_WhenStoredMatchesInput_ReturnsTrue() {
        CaptchaUtil.storeCaptcha("1234");
        assertThat(CaptchaUtil.verifyCaptcha("1234")).isTrue();
    }

    @Test
    void verifyCaptcha_WhenInputIsWrong_ReturnsFalse() {
        CaptchaUtil.storeCaptcha("1234");
        assertThat(CaptchaUtil.verifyCaptcha("9999")).isFalse();
    }
}
