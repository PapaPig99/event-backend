package com.example.eventproject.controller;

import com.example.eventproject.dto.AuthResponse;
import com.example.eventproject.dto.LoginRequest;
import com.example.eventproject.dto.RegisterRequest;
import com.example.eventproject.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * เทสให้ตรงกับ AuthController ปัจจุบัน โดยไม่ใช้ @MockBean (deprecated)
 * แนวทาง: สร้าง mock เป็น Spring bean ผ่าน @TestConfiguration + @Import
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.MockConfig.class)
class AuthControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        AuthService authService() {
            // สร้าง Mockito mock แล้วใส่เป็น Spring bean
            return Mockito.mock(AuthService.class);
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ได้เป็น bean จริง (ที่เป็น mock) จาก MockConfig
    @Autowired AuthService auth;

    @Test
    @DisplayName("POST /api/auth/register → 200 OK + JSON")
    void register_shouldReturnOk() throws Exception {
        // ปรับตาม signature ของ record RegisterRequest ของคุณ
        // สมมุติว่าเป็น (email, password, name, phone, role)
        RegisterRequest req = new RegisterRequest(
                "a@b.com",
                "123456",
                "Alice",
                "0812345678",
                "USER"
        );

        // ปรับตาม signature ของ record AuthResponse ของคุณ
        // สมมุติว่าเป็น (token, userId, email, name, role)
        AuthResponse resp = new AuthResponse(
                "fake-jwt",
                1L,
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
                1L,
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
