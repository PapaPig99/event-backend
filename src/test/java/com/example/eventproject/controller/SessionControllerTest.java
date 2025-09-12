package com.example.eventproject.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.isEmptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test สำหรับ SessionController
 *
 * ใช้ @WebMvcTest เพื่อทดสอบเฉพาะชั้น Web (Controller) — ไม่โหลด Bean ทั้งแอปให้ช้า
 * ใช้ @AutoConfigureMockMvc(addFilters = false) เพื่อปิด security filters กัน 401/403
 *
 * ตอนนี้ Controller คืนแค่ status โดยไม่มี body → เช็คเพียงสถานะ + body ว่าง
 */
@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/events/{eventId}/sessions → 200 OK")
    void listByEvent_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/events/{eventId}/sessions", 123))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @DisplayName("POST /api/events/{eventId}/sessions → 201 Created")
    void create_shouldReturn201() throws Exception {
        // ใส่ JSON ตามที่ระบบจริงต้องการ ในตอนนี้ใช้โครงง่าย ๆ ไปก่อน
        String body = """
          {"startTime":"2025-07-19T18:00:00","endTime":"2025-07-19T21:00:00"}
        """;

        mockMvc.perform(post("/api/events/{eventId}/sessions", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @DisplayName("PUT /api/sessions/{id} → 200 OK")
    void update_shouldReturn200() throws Exception {
        String body = """
          {"startTime":"2025-07-20T18:00:00","endTime":"2025-07-20T21:00:00"}
        """;

        mockMvc.perform(put("/api/sessions/{id}", 456)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @DisplayName("DELETE /api/sessions/{id} → 204 No Content")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/sessions/{id}", 456))
                .andExpect(status().isNoContent())
                .andExpect(content().string(isEmptyString()));
    }
}
