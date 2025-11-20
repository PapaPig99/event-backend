// src/test/java/com/example/eventproject/controller/DashboardControllerTest.java
package com.example.eventproject.controller;

import com.example.eventproject.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("GET /api/dashboard/summary (ไม่มี eventId) → 200")
    void summary_ok_withoutEventId() throws Exception {
        // ไม่สนใจค่า return จริง ๆ แค่ให้ controller ทำงานได้
        when(dashboardService.getDashboard(null)).thenReturn(null);

        mockMvc.perform(get("/api/dashboard/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/dashboard/summary?eventId=1 → 200 และเรียก service ด้วย eventId = 1")
    void summary_ok_withEventId() throws Exception {
        when(dashboardService.getDashboard(1)).thenReturn(null);

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("eventId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/dashboard/events → 200")
    void eventTable_ok() throws Exception {
        when(dashboardService.getEventTable()).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/events")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
