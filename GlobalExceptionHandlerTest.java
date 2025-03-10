package com.example.lab1;

import com.example.lab1.exception.BadRequestException;
import com.example.lab1.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleBadRequestException_ReturnsBadRequestWithMessage() {
        BadRequestException ex = new BadRequestException("用户名已存在");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadRequestException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("用户名已存在", response.getBody().get("error"));
    }

    @Test
    void handleValidationExceptions_ReturnsBadRequestWithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("objectName", "username", "用户名不能为空");
        when(ex.getBindingResult().getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("用户名不能为空", response.getBody().get("username"));
    }
}
