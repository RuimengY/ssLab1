package com.example.lab1;

import com.example.lab1.dto.LoginRequest;
import com.example.lab1.dto.NewUserRequest;
import com.example.lab1.dto.UserResponse;
import com.example.lab1.entity.User;
import com.example.lab1.exception.BadRequestException;
import com.example.lab1.repository.UserRepository;
import com.example.lab1.utils.CaptchaUtil;
import com.example.lab1.utils.JWTUtil;
import com.example.lab1.utils.PasswordUtil;
import com.example.lab1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private NewUserRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "encodedPassword");
        testUser.setId(1L);

        registerRequest = new NewUserRequest(
                "testUser", "Pass123", "captcha-id", "123456");

        loginRequest = new LoginRequest(
                "testUser", "Pass123", "captcha-id", "123456");
    }

    @Test
    void registerUser_Success() {
        // 模拟验证码验证和密码加密
        try (MockedStatic<CaptchaUtil> captchaUtil = Mockito.mockStatic(CaptchaUtil.class);
             MockedStatic<PasswordUtil> passwordUtil = Mockito.mockStatic(PasswordUtil.class)) {

            captchaUtil.when(() -> CaptchaUtil.validateCaptcha(anyString(), anyString())).thenReturn(true);
            passwordUtil.when(() -> PasswordUtil.encryptPassword(anyString())).thenReturn("encodedPassword");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // 执行测试
            UserResponse response = userService.registerUser(registerRequest);

            // 验证结果
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("testUser", response.username());

            // 验证方法调用
            verify(userRepository).existsByUsername("testUser");
            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void registerUser_InvalidCaptcha() {
        // 模拟验证码验证失败
        try (MockedStatic<CaptchaUtil> captchaUtil = Mockito.mockStatic(CaptchaUtil.class)) {
            captchaUtil.when(() -> CaptchaUtil.validateCaptcha(anyString(), anyString())).thenReturn(false);

            // 执行测试并验证异常
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> userService.registerUser(registerRequest));

            assertEquals("验证码错误", exception.getMessage());

            // 验证存储库方法未被调用
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void registerUser_UsernameExists() {
        // 模拟验证码验证成功但用户名已存在
        try (MockedStatic<CaptchaUtil> captchaUtil = Mockito.mockStatic(CaptchaUtil.class)) {
            captchaUtil.when(() -> CaptchaUtil.validateCaptcha(anyString(), anyString())).thenReturn(true);
            when(userRepository.existsByUsername(anyString())).thenReturn(true);

            // 执行测试并验证异常
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> userService.registerUser(registerRequest));

            assertEquals("用户名已存在", exception.getMessage());

            // 验证方法调用
            verify(userRepository).existsByUsername("testUser");
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void loginUser_Success() {
        // 模拟验证码验证、用户查找、密码检查和令牌生成
        try (MockedStatic<CaptchaUtil> captchaUtil = Mockito.mockStatic(CaptchaUtil.class);
             MockedStatic<PasswordUtil> passwordUtil = Mockito.mockStatic(PasswordUtil.class);
             MockedStatic<JWTUtil> jwtUtil = Mockito.mockStatic(JWTUtil.class)) {

            captchaUtil.when(() -> CaptchaUtil.validateCaptcha(anyString(), anyString())).thenReturn(true);
            passwordUtil.when(() -> PasswordUtil.checkPassword(anyString(), anyString())).thenReturn(true);
            jwtUtil.when(() -> JWTUtil.generateToken(anyString())).thenReturn("mock-jwt-token");

            when(userRepository.findByUsername(anyString())).thenReturn(testUser);

            // 执行测试
            String token = userService.loginUser(loginRequest);

            // 验证结果
            assertEquals("mock-jwt-token", token);

            // 验证方法调用
            verify(userRepository).findByUsername("testUser");
        }
    }

    @Test
    void loginUser_InvalidCaptcha() {
        // 模拟验证码验证失败
        try (MockedStatic<CaptchaUtil> captchaUtil = Mockito.mockStatic(CaptchaUtil.class)) {
            captchaUtil.when(() -> CaptchaUtil.validateCaptcha(anyString(), anyString())).thenReturn(false);

            // 执行测试并验证异常
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> userService.loginUser(loginRequest));

            assertEquals("验证码错误", exception.getMessage());

            // 验证存储库方法未被调用
            verify(userRepository, never()).findByUsername(anyString());
        }
    }

    @Test
    void loginUser_InvalidCredentials() {
        // 模拟验证码验证成功但用户名或密码错误
        try (MockedStatic<CaptchaUtil> captchaUtil = Mockito.mockStatic(CaptchaUtil.class);
             MockedStatic<PasswordUtil> passwordUtil = Mockito.mockStatic(PasswordUtil.class)) {

            captchaUtil.when(() -> CaptchaUtil.validateCaptcha(anyString(), anyString())).thenReturn(true);
            passwordUtil.when(() -> PasswordUtil.checkPassword(anyString(), anyString())).thenReturn(false);

            when(userRepository.findByUsername(anyString())).thenReturn(testUser);

            // 执行测试并验证异常
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> userService.loginUser(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());

            // 验证方法调用
            verify(userRepository).findByUsername("testUser");
        }
    }

    @Test
    void validateToken_Valid() {
        // 模拟JWT验证
        try (MockedStatic<JWTUtil> jwtUtil = Mockito.mockStatic(JWTUtil.class)) {
            jwtUtil.when(() -> JWTUtil.validateToken(anyString())).thenReturn(true);

            // 执行测试
            boolean result = userService.validateToken("mock-jwt-token");

            // 验证结果
            assertTrue(result);
        }
    }

    @Test
    void validateToken_Invalid() {
        // 模拟JWT验证失败
        try (MockedStatic<JWTUtil> jwtUtil = Mockito.mockStatic(JWTUtil.class)) {
            jwtUtil.when(() -> JWTUtil.validateToken(anyString())).thenThrow(new RuntimeException("Invalid token"));

            // 执行测试
            boolean result = userService.validateToken("invalid-token");

            // 验证结果
            assertFalse(result);
        }
    }

    @Test
    void getUserProfileByToken_Success() {
        // 模拟JWT解析和用户查找
        try (MockedStatic<JWTUtil> jwtUtil = Mockito.mockStatic(JWTUtil.class)) {
            jwtUtil.when(() -> JWTUtil.getUsernameFromToken(anyString())).thenReturn("testUser");
            when(userRepository.findByUsername(anyString())).thenReturn(testUser);

            // 执行测试
            UserResponse response = userService.getUserProfileByToken("mock-jwt-token");

            // 验证结果
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("testUser", response.username());

            // 验证方法调用
            verify(userRepository).findByUsername("testUser");
        }
    }

    @Test
    void getUserProfileByToken_UserNotFound() {
        // 模拟JWT解析成功但用户不存在
        try (MockedStatic<JWTUtil> jwtUtil = Mockito.mockStatic(JWTUtil.class)) {
            jwtUtil.when(() -> JWTUtil.getUsernameFromToken(anyString())).thenReturn("testUser");
            when(userRepository.findByUsername(anyString())).thenReturn(null);

            // 执行测试并验证异常
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userService.getUserProfileByToken("mock-jwt-token"));

            assertEquals("404 NOT_FOUND \"用户不存在\"", exception.getMessage());

            // 验证方法调用
            verify(userRepository).findByUsername("testUser");
        }
    }

    @Test
    void getUserProfileByToken_InvalidToken() {
        // 模拟JWT解析失败
        try (MockedStatic<JWTUtil> jwtUtil = Mockito.mockStatic(JWTUtil.class)) {
            jwtUtil.when(() -> JWTUtil.getUsernameFromToken(anyString())).thenThrow(new RuntimeException("Invalid token"));

            // 执行测试并验证异常
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userService.getUserProfileByToken("invalid-token"));

            assertEquals("401 UNAUTHORIZED \"无效的Token\"", exception.getMessage());

            // 验证存储库方法未被调用
            verify(userRepository, never()).findByUsername(anyString());
        }
    }
}

