package com.example.lab1;


import com.example.lab1.dto.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {
    private Validator validator;

    //作用：在每个测试方法执行之前初始化 validator
    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void whenValidLoginRequest_thenNoViolations() {
        LoginRequest request = new LoginRequest("validUser", "validPass123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);//使用校验器对 request 进行校验，并将结果存储在 violations 中
        assertThat(violations).isEmpty();//使用 AssertJ 断言 violations 为空，表示没有校验错误。
    }

    @Test
    void whenUsernameIsBlank_thenOneViolation() {
        LoginRequest request = new LoginRequest("", "validPass123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("用户名不能为空");
    }

    @Test
    void whenPasswordIsBlank_thenOneViolation() {
        LoginRequest request = new LoginRequest("validUser", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("密码不能为空");
    }
}
