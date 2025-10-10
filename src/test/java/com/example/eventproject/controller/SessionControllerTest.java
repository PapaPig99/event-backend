package com.example.eventproject.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Unit tests for SessionController
 * - ใช้ @WebMvcTest เฉพาะ Controller
 * - ปิด Security filters เพื่อไม่ให้ 401/403 มารบกวน
 * - ระวัง: เมธอดที่ไม่มี body จะไม่ส่ง Content-Type กลับมา
 */
@WebMvcTest(controllers = SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /* =========================
     *           READ
     * ========================= */
    @Test
    @DisplayName("GET /api/events/{eventId}/sessions → 200 (body ว่าง)")
    void listByEvent_ok_emptyBody() throws Exception {
        mockMvc.perform(get("/api/events/{eventId}/sessions", 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // ไม่มี body ก็พอ
        // ถ้าต้องการยืนยันว่าไม่มี Content-Type:
        // .andExpect(header().doesNotExist("Content-Type"));
    }

    /* =========================
     *          CREATE
     * ========================= */
    @Test
    @DisplayName("POST /api/events/{eventId}/sessions (JSON) → 201 Created")
    void create_ok_201_json() throws Exception {
        String json = """
            { "title": "Morning Show", "startsAt": "2025-12-01T09:00:00", "endsAt": "2025-12-01T11:00:00" }
            """;
        mockMvc.perform(post("/api/events/{eventId}/sessions", 77)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
        // ตามโค้ด controller: ไม่มี body → ไม่ assert Content-Type
    }

    @Test
    @DisplayName("POST /api/events/{eventId}/sessions ขาด Content-Type → 415 Unsupported Media Type")
    void create_missing_content_type_415() throws Exception {
        // เพราะ @PostMapping ระบุ consumes = application/json
        mockMvc.perform(post("/api/events/{eventId}/sessions", 77)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    /* =========================
     *          UPDATE
     * ========================= */
    @Test
    @DisplayName("PUT /api/sessions/{id} (JSON) → 200 OK")
    void update_ok_200_json() throws Exception {
        String json = """
            { "title": "Updated Session", "startsAt": "2025-12-01T10:00:00", "endsAt": "2025-12-01T12:00:00" }
            """;
        mockMvc.perform(put("/api/sessions/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("PUT /api/sessions/{id} ขาด Content-Type → 415 Unsupported Media Type")
    void update_missing_content_type_415() throws Exception {
        mockMvc.perform(put("/api/sessions/{id}", 123)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    /* =========================
     *          DELETE
     * ========================= */
    @Test
    @DisplayName("DELETE /api/sessions/{id} → 204 No Content")
    void delete_ok_204() throws Exception {
        mockMvc.perform(delete("/api/sessions/{id}", 999))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    /* =========================
     *    METHOD NOT ALLOWED / 404
     * ========================= */
    @Test
    @DisplayName("GET /api/sessions/{id} → 405 Method Not Allowed (ไม่มี mapping GET ที่ /sessions/{id})")
    void get_single_405() throws Exception {
        mockMvc.perform(get("/api/sessions/{id}", 1))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST /api/sessions/{id} → 405 Method Not Allowed")
    void post_to_update_endpoint_405() throws Exception {
        mockMvc.perform(post("/api/sessions/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("พิมพ์ path ผิด: GET /api/event/{id}/sessions → 404 Not Found")
    void typo_path_404() throws Exception {
        mockMvc.perform(get("/api/event/{id}/sessions", 10)) // event (ไม่มี s)
                .andExpect(status().isNotFound());
    }
}
