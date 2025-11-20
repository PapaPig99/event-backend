// src/test/java/com/example/eventproject/controller/AuthControllerTest.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.AuthResponse;
import com.example.eventproject.dto.LoginRequest;
import com.example.eventproject.dto.RegisterRequest;
import com.example.eventproject.repository.UserRepository;
import com.example.eventproject.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ให้ Spring สร้าง mock ให้ทั้งสองตัวที่ AuthController ต้องการ
    @MockBean
    AuthService auth;

    @MockBean
    UserRepository userRepository;

    @Test
    @DisplayName("POST /api/auth/register → 200 OK + JSON")
    void register_shouldReturnOk() throws Exception {

        RegisterRequest req = new RegisterRequest(
                "a@b.com",
                "123456",
                "Alice"
        );

        AuthResponse resp = new AuthResponse(
                "fake-jwt",
                "a@b.com",
                "Alice",
                "USER"
        );

        when(auth.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("fake-jwt"))
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login → 200 OK + JSON")
    void login_shouldReturnOk() throws Exception {

        LoginRequest req = new LoginRequest(
                "a@b.com",
                "123456"
        );

        AuthResponse resp = new AuthResponse(
                "fake-jwt-login",
                "a@b.com",
                "Alice",
                "USER"
        );

        when(auth.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("fake-jwt-login"))
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }
}
