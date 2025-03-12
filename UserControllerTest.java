package com.example.lab1;

import com.example.lab1.dto.LoginRequest;
import com.example.lab1.dto.NewUserRequest;
import com.example.lab1.dto.UserResponse;
import com.example.lab1.service.UserService;
import com.example.lab1.controller.UserController;
import com.example.lab1.utils.CaptchaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse mockUserResponse;
    private String mockToken;

    @BeforeEach
    void setUp() {
        mockUserResponse = new UserResponse(1L, "testUser");
        mockToken = "mock-jwt-token";
    }

    @Test
    void registerUser_Success() throws Exception {
        // 准备测试数据
        NewUserRequest request = new NewUserRequest(
                "testUser", "Pass123", "captcha-id", "123456");

        // 模拟服务行为
        when(userService.registerUser(any(NewUserRequest.class))).thenReturn(mockUserResponse);

        // 执行测试
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void loginUser_Success() throws Exception {
        // 准备测试数据
        LoginRequest request = new LoginRequest(
                "testUser", "Pass123", "captcha-id", "123456");

        // 模拟服务行为
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(mockToken);

        // 执行测试
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    @Test
    void getCaptcha_Success() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/users/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captchaId").exists())
                .andExpect(jsonPath("$.captchaImage").exists());
    }

    @Test
    void validateToken_Valid() throws Exception {
        // 模拟服务行为
        when(userService.validateToken(anyString())).thenReturn(true);

        // 执行测试
        mockMvc.perform(get("/api/users/validate-token")
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateToken_Invalid() throws Exception {
        // 模拟服务行为
        when(userService.validateToken(anyString())).thenReturn(false);

        // 执行测试
        mockMvc.perform(get("/api/users/validate-token")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void getUserProfile_Success() throws Exception {
        // 模拟服务行为
        when(userService.getUserProfileByToken(anyString())).thenReturn(mockUserResponse);

        // 执行测试
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"));
    }
}