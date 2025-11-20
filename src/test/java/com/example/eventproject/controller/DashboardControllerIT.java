// src/test/java/com/example/eventproject/controller/DashboardControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.DashboardDto;
import com.example.eventproject.dto.EventSalesSummary;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerIT {

    @Autowired
    MockMvc mvc;

    @MockBean
    DashboardService service;

    @Test
    @DisplayName("GET /api/dashboard/summary → 200 + JSON fields ถูกต้อง")
    void summary_ok_200() throws Exception {

        DashboardDto dto = new DashboardDto(
                5L,    // activeEvents
                1200L, // ticketsSold
                1500L, // totalRegistrations
                1500L, // totalSignups
                300L,  // dropOffs
                80L,   // showRate
                1000L  // checkIn
        );

        when(service.getDashboard(null)).thenReturn(dto);

        mvc.perform(get("/api/dashboard/summary")
                        .with(user("admin").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activeEvents").value(5))
                .andExpect(jsonPath("$.ticketsSold").value(1200))
                .andExpect(jsonPath("$.totalRegistrations").value(1500))
                .andExpect(jsonPath("$.totalSignups").value(1500))
                .andExpect(jsonPath("$.dropOffs").value(300))
                .andExpect(jsonPath("$.showRate").value(80))
                .andExpect(jsonPath("$.checkIn").value(1000));
    }

    @Test
    @DisplayName("GET /api/dashboard/events → 200 + list EventSalesSummary (array มี 1 element)")
    void eventTable_ok_200() throws Exception {

        List<EventSalesSummary> table = List.of(
                // แค่สร้าง object ให้ controller ส่งออก ไม่ได้เช็คชื่อ field
                new EventSalesSummary(1, "Music Fest", "Festival", 1000L, 850L)
        );

        when(service.getEventTable()).thenReturn(table);

        mvc.perform(get("/api/dashboard/events")
                        .with(user("admin").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // เช็คว่าเป็น array
                .andExpect(jsonPath("$").isArray())
                // และมี 1 element
                .andExpect(jsonPath("$.length()").value(1));
    }
}
