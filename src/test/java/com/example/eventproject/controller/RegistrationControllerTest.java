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
 * การทดสอบ Unit Test สำหรับ RegistrationController
 * โดยใช้ @WebMvcTest จะโหลดเฉพาะ Controller ชั้น Web เท่านั้น
 * ไม่ต้องโหลด Bean ทั้ง Application
 */
@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false) // ปิด Security filter (กันไม่ให้เจอ 401/403 ระหว่างเทส)
class RegistrationControllerTest {

    // MockMvc เป็น class ที่ช่วยจำลองการยิง HTTP request ไปยัง Controller
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/registrations → 201 Created")
    void create_shouldReturn201() throws Exception {
        // เตรียม body JSON ตัวอย่างที่ส่งไป
        String body = """
          {"eventId": 1, "userId": 99}
        """;

        // ใช้ mockMvc.perform() เพื่อจำลองการยิง POST
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON) // header Content-Type
                        .header("Idempotency-Key", "abc-123")     // header เพิ่มเติม
                        .content(body))                           // body ที่ส่ง
                .andExpect(status().isCreated())          // ตรวจว่า HTTP 201
                .andExpect(content().string(isEmptyString())); // ตอนนี้ยังไม่มี body กลับมา
    }

    @Test
    @DisplayName("GET /api/registrations/me → 200 OK")
    void myRegistrations_shouldReturn200() throws Exception {
        // ทดสอบ endpoint ที่มี query parameters
        mockMvc.perform(get("/api/registrations/me")
                        .param("status", "CONFIRMED")
                        .param("from", "2025-01-01")
                        .param("to", "2025-12-31"))
                .andExpect(status().isOk())               // ตรวจว่า HTTP 200
                .andExpect(content().string(isEmptyString())); // body ยังว่าง
    }

    @Test
    @DisplayName("PATCH /api/registrations/{id}/cancel → 200 OK")
    void cancel_shouldReturn200() throws Exception {
        // body JSON ที่ส่งในการ cancel
        String body = """
          {"reason": "User request"}
        """;

        mockMvc.perform(patch("/api/registrations/{id}/cancel", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())               // ตรวจว่า HTTP 200
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @DisplayName("PATCH /api/registrations/{id}/confirm → 200 OK")
    void confirm_shouldReturn200() throws Exception {
        // body JSON ที่ส่งในการ confirm
        String body = """
          {"paymentId": "pay-2025"}
        """;

        mockMvc.perform(patch("/api/registrations/{id}/confirm", 20)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())               // ตรวจว่า HTTP 200
                .andExpect(content().string(isEmptyString()));
    }
}
