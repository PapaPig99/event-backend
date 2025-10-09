package com.example.eventproject.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Single-file, single-class test for AdminController
 * - ปิด Security filters เพื่อไม่ให้ 401/403 มารบกวนพฤติกรรมของ Controller
 * - เช็ก JSON และสถานะให้ “เป๊ะ” ตามที่ต้องการ
 */
@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/admin/dashboard → 200 + JSON ตรงสเปก")
    void get_dashboard_ok_strict() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("admin only"))
                .andExpect(jsonPath("$.*", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/admin/dashboard → 405 Method Not Allowed")
    void post_dashboard_405() throws Exception {
        mockMvc.perform(post("/api/admin/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("PUT /api/admin/dashboard → 405 Method Not Allowed")
    void put_dashboard_405() throws Exception {
        mockMvc.perform(put("/api/admin/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("PATCH /api/admin/dashboard → 405 Method Not Allowed")
    void patch_dashboard_405() throws Exception {
        mockMvc.perform(patch("/api/admin/dashboard").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("DELETE /api/admin/dashboard → 405 Method Not Allowed")
    void delete_dashboard_405() throws Exception {
        mockMvc.perform(delete("/api/admin/dashboard"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /api/admin/dash (สะกดผิด) → 404 Not Found")
    void typo_path_404() throws Exception {
        mockMvc.perform(get("/api/admin/dash"))
                .andExpect(status().isNotFound());
    }
}
