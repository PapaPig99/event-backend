package com.example.eventproject.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ทดสอบเฉพาะชั้น Controller (ไม่ต้องขึ้น Context ทั้งแอป)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/auth/login → 200 OK")
    void login_shouldReturnOk() throws Exception {
        String body = """
            {"username":"alice","password":"secret"}
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // ตอนนี้ controller ยังไม่คืน body
    }

    @Test
    @DisplayName("POST /api/auth/logout → 204 No Content")
    void logout_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /api/auth/register → 201 Created")
    void register_shouldReturnCreated() throws Exception {
        String body = """
            {"email":"a@example.com","password":"secret","name":"Alice"}
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/users/me → 200 OK")
    void me_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
