package com.example.lab1;

import com.example.lab1.dto.LoginRequest;
import com.example.lab1.dto.NewUserRequest;
import com.example.lab1.dto.UserResponse;
import com.example.lab1.entity.User;
import com.example.lab1.exception.BadRequestException;
import com.example.lab1.repository.UserRepository;
import com.example.lab1.utils.CaptchaUtil;
import com.example.lab1.utils.JwtUtil;
import com.example.lab1.utils.PasswordUtil;
import com.example.lab1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


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

    private final String testUsername = "testUser";
    private final String testPassword = "password123";
    private final String testCaptcha = "123456";
    private final String encryptedPassword = "encryptedPassword123";
    private final String jwtToken = "jwtToken123";

    @BeforeEach
    void setUp() {
        // 不需要额外设置，@Mock和@InjectMocks已经处理了基本的依赖注入
    }

    @Test
    void registerUser_Success() {
        // 准备测试数据
        NewUserRequest request = new NewUserRequest(testUsername, testPassword, testCaptcha);
        User savedUser = new User(testUsername, encryptedPassword);

        // 配置Mock行为
        try (MockedStatic<CaptchaUtil> captchaUtilMock = mockStatic(CaptchaUtil.class);
             MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {

            captchaUtilMock.when(() -> CaptchaUtil.verifyCaptcha(testCaptcha)).thenReturn(true);
            passwordUtilMock.when(() -> PasswordUtil.encryptPassword(testPassword)).thenReturn(encryptedPassword);

            when(userRepository.existsByUsername(testUsername)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // 执行测试
            UserResponse response = userService.registerUser(request);

            // 验证结果
            assertNotNull(response);
            assertEquals(testUsername, response.username());

            // 验证交互
            verify(userRepository).existsByUsername(testUsername);
            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void registerUser_InvalidCaptcha() {
        // 准备测试数据
        NewUserRequest request = new NewUserRequest(testUsername, testPassword, testCaptcha);

        // 配置Mock行为
        try (MockedStatic<CaptchaUtil> captchaUtilMock = mockStatic(CaptchaUtil.class)) {
            captchaUtilMock.when(() -> CaptchaUtil.verifyCaptcha(testCaptcha)).thenReturn(false);

            // 执行测试并验证结果
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                userService.registerUser(request);
            });

            assertEquals("验证码错误", exception.getMessage());

            // 验证交互
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void registerUser_DuplicateUsername() {
        // 准备测试数据
        NewUserRequest request = new NewUserRequest(testUsername, testPassword, testCaptcha);

        // 配置Mock行为
        try (MockedStatic<CaptchaUtil> captchaUtilMock = mockStatic(CaptchaUtil.class)) {
            captchaUtilMock.when(() -> CaptchaUtil.verifyCaptcha(testCaptcha)).thenReturn(true);
            when(userRepository.existsByUsername(testUsername)).thenReturn(true);

            // 执行测试并验证结果
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                userService.registerUser(request);
            });

            assertEquals("用户名已存在", exception.getMessage());

            // 验证交互
            verify(userRepository).existsByUsername(testUsername);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void loginUser_Success() {
        // 准备测试数据
        LoginRequest request = new LoginRequest(testUsername, testPassword);
        User user = new User(testUsername, encryptedPassword);

        // 配置Mock行为
        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class);
             MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {

            when(userRepository.findByUsername(testUsername)).thenReturn(user);
            passwordUtilMock.when(() -> PasswordUtil.checkPassword(testPassword, encryptedPassword)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.generateToken(testUsername)).thenReturn(jwtToken);

            // 执行测试
            String token = userService.loginUser(request);

            // 验证结果
            assertNotNull(token);
            assertEquals(jwtToken, token);

            // 验证交互
            verify(userRepository).findByUsername(testUsername);
        }
    }

    @Test
    void loginUser_InvalidCredentials_UserNotFound() {
        // 准备测试数据
        LoginRequest request = new LoginRequest(testUsername, testPassword);

        // 配置Mock行为
        when(userRepository.findByUsername(testUsername)).thenReturn(null);

        // 执行测试并验证结果
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.loginUser(request);
        });

        assertEquals("用户名或密码错误", exception.getMessage());

        // 验证交互
        verify(userRepository).findByUsername(testUsername);
    }

    @Test
    void loginUser_InvalidCredentials_WrongPassword() {
        // 准备测试数据
        LoginRequest request = new LoginRequest(testUsername, testPassword);
        User user = new User(testUsername, encryptedPassword);

        // 配置Mock行为
        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {
            when(userRepository.findByUsername(testUsername)).thenReturn(user);
            passwordUtilMock.when(() -> PasswordUtil.checkPassword(testPassword, encryptedPassword)).thenReturn(false);

            // 执行测试并验证结果
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                userService.loginUser(request);
            });

            assertEquals("用户名或密码错误", exception.getMessage());

            // 验证交互
            verify(userRepository).findByUsername(testUsername);
        }
    }
}
