package com.example.eventproject.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.isEmptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test สำหรับ DashboardController
 * - ใช้ @WebMvcTest เพื่อโหลดเฉพาะชั้น Web (Controller)
 * - ปิด security filters ชั่วคราว (addFilters=false) กัน 401/403 ระหว่างยังไม่ตั้งค่า auth จริง
 * - ตอนนี้ controller ยังไม่คืน body → เช็คเพียง status 200 + body ว่าง
 */
@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/dashboard/summary → 200 OK")
    void summary_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @DisplayName("GET /api/dashboard/sales-progress → 200 OK")
    void salesProgress_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/dashboard/sales-progress"))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @DisplayName("GET /api/events/{eventId}/analytics → 200 OK (มี/ไม่มี sessionId ก็ได้)")
    void analytics_shouldReturn200() throws Exception {
        // กรณีไม่มี sessionId
        mockMvc.perform(get("/api/events/{eventId}/analytics", 123))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));

        // กรณีมี sessionId
        mockMvc.perform(get("/api/events/{eventId}/analytics", 123)
                        .param("sessionId", "456"))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }
}
