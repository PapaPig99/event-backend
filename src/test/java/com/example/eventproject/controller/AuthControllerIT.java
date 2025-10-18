// src/test/java/com/example/eventproject/controller/AuthControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.AuthResponse;
import com.example.eventproject.service.AuthService;
import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;

    // 👇 Mock AuthService เพื่อไม่เรียกของจริง
    @MockBean AuthService auth;

    @Test
    void register_ok_200() throws Exception {
        // คืน AuthResponse แบบ mock (ไม่ต้องรู้ constructor ภายใน)
        when(auth.register(any())).thenReturn(mock(AuthResponse.class));

        String body = "{}"; // ให้ Jackson สร้าง DTO เปล่าได้พอ (controller ไม่มี @Valid)

        mvc.perform(post("/api/auth/register")
                        .with(csrf()) // กันกรณีเปิด CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk()); // controller ใช้ ResponseEntity.ok(...)
    }

    @Test
    void login_ok_200() throws Exception {
        when(auth.login(any())).thenReturn(mock(AuthResponse.class));

        String body = "{}";

        mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
