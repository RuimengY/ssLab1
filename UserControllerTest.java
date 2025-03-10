package com.example.lab1;

import com.example.lab1.dto.LoginRequest;
import com.example.lab1.dto.NewUserRequest;
import com.example.lab1.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@SpringBootTest：这个注解告诉Spring Boot在运行测试时要加载整个应用程序上下文。
//@AutoConfigureMockMvc：这个注解自动配置MockMvc，用于模拟HTTP请求
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 添加事务注解，使每个测试方法都在一个事务中运行，运行结束后自动回滚。
public class UserControllerTest {
    /* mockMvc：用于模拟HTTP请求和验证响应。
       userService：用于调用服务层的方法。
       objectMapper：用于将Java对象转换为JSON字符串，或者将JSON字符串转换为Java对象。
     */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /* @BeforeEach：这个注解表示在每个测试方法执行之前都会执行setUp方法。
    setUp：在这个方法中，生成了一个固定的验证码1234，并将其存储起来，以便在后续的测试中使用。
     */
    @BeforeEach
    public void setUp() {
        // 在每个测试前生成并存储验证码
        String captcha = "1234";
        com.example.lab1.utils.CaptchaUtil.storeCaptcha(captcha);
    }

    // @Test：这个注解表示这是一个测试方法。
    // 成功注册用户
    @Test
    public void testRegisterUserSuccess() throws Exception {
        NewUserRequest request = new NewUserRequest("test123", "abc123", "1234"); //创建一个新的用户注册请求对象

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) //模拟一个POST请求到/users/register，并发送JSON格式的请求体。
                .andExpect(status().isOk())                 //期望响应状态码为200（OK）。
                .andExpect(jsonPath("$.username").value("test123")) //期望返回的JSON中username字段的值为test123。
                .andExpect(jsonPath("$.id").isNumber()); //期望返回的JSON中id字段的值为数字。
    }

    // 不能注册用户名重复的用户
    @Test
    public void testRegisterUserWithDuplicateUsername() throws Exception {
        userService.registerUser(new NewUserRequest("duplicate", "abc123", "1234"));

        NewUserRequest request = new NewUserRequest("duplicate", "xyz789", "1234");
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))  //再次尝试注册一个用户名为duplicate的用户
                .andExpect(status().isBadRequest())   //期望响应状态码为400（Bad Request）
                .andExpect(jsonPath("$.error").value("用户名已存在")); //期望返回的JSON中error字段的值为"用户名已存在"。
    }

    // 不能注册验证码错误的用户
    @Test
    public void testRegisterUserWithInvalidCaptcha() throws Exception {
        NewUserRequest request = new NewUserRequest("test456", "abc123", "9999"); //创建一个新的用户注册请求对象，验证码为9999（与之前存储的1234不匹配）。

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("验证码错误"));
    }

    // 登录成功
    @Test
    public void testLoginUserSuccess() throws Exception {
        userService.registerUser(new NewUserRequest("loginuser", "abc123", "1234"));

        LoginRequest request = new LoginRequest("loginuser", "abc123");
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(is(not("")))) // 验证非空字符串
                .andExpect(content().string(matchesRegex("\\S+"))); // 验证非空白字符串

    }

    // 不能登录用户名不存在的用户
    @Test
    public void testLoginUserWithWrongPassword() throws Exception {
        userService.registerUser(new NewUserRequest("loginuser2", "abc123", "1234"));

        LoginRequest request = new LoginRequest("loginuser2", "wrongpass");
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    // 检测是否生成正确的验证码
    @Test
    public void testGetCaptcha() throws Exception {
        mockMvc.perform(get("/users/captcha")) //模拟一个GET请求到/users/captcha
                .andExpect(status().isOk())     //期望响应状态码为200（OK）
                .andExpect(content().string(matchesRegex("\\d{4}"))); //期望返回的内容是一个4位数字的字符串
    }
}