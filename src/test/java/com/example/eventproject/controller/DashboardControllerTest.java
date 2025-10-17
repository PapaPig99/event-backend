package com.example.eventproject.controller;

import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private OverviewResponse mockOverview(long activeEvents, long ticketsSold,
                                          List<EventSalesSummary> salesProgress) {
        OverviewResponse overview = Mockito.mock(OverviewResponse.class);
        when(overview.getSalesProgress()).thenReturn(salesProgress);
        when(overview.getActiveEvents()).thenReturn(activeEvents);
        when(overview.getTicketsSold()).thenReturn(ticketsSold);
        return overview;
    }

    private EventSalesSummary mockSales(int id, String title, String category, long capacity, long sold) {
        EventSalesSummary summary = Mockito.mock(EventSalesSummary.class);
        when(summary.getId()).thenReturn(id);
        when(summary.getTitle()).thenReturn(title);
        when(summary.getCategory()).thenReturn(category);
        when(summary.getCapacity()).thenReturn(capacity);
        when(summary.getSold()).thenReturn(sold);
        return summary;
    }

    @Test
    @DisplayName("GET /api/dashboard/summary → 200 + JSON Overview ถูกต้อง")
    void summary_ok() throws Exception {
        var s1 = mockSales(101, "Dev Summit", "Tech", 500L, 120L);
        var s2 = mockSales(202, "PyCon TH", "Programming", 300L, 200L);
        var overview = mockOverview(7L, 320L, List.of(s1, s2));

        when(dashboardService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/dashboard/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.salesProgress", hasSize(2)));

        verify(dashboardService, times(1)).getOverview();
    }

    @Test
    @DisplayName("GET /api/dashboard/sales-progress → 200 + array ของ EventSalesSummary")
    void sales_progress_ok() throws Exception {
        var s1 = mockSales(101, "Dev Summit", "Tech", 500L, 120L);
        var s2 = mockSales(202, "PyCon TH", "Programming", 300L, 200L);
        var overview = mockOverview(7L, 320L, List.of(s1, s2));

        when(dashboardService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/dashboard/sales-progress").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Dev Summit"))
                .andExpect(jsonPath("$[0].category").value("Tech"))
                .andExpect(jsonPath("$[0].capacity").value(500))
                .andExpect(jsonPath("$[0].sold").value(120))
                .andExpect(jsonPath("$[1].title").value("PyCon TH"))
                .andExpect(jsonPath("$[1].category").value("Programming"))
                .andExpect(jsonPath("$[1].capacity").value(300))
                .andExpect(jsonPath("$[1].sold").value(200));

        verify(dashboardService, times(1)).getOverview();
    }

    @Test
    @DisplayName("GET /api/events/{id}/analytics → 200 (body ว่าง)")
    void analytics_ok() throws Exception {
        mockMvc.perform(get("/api/events/{eventId}/analytics", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // ไม่มี body → ไม่มี Content-Type ด้วย เป็นพฤติกรรมปกติของ Spring
                .andExpect(content().string("")); // body ว่าง
        // ถ้าอยากเช็ค header ว่าไม่มีจริง ๆ ก็เพิ่ม:
        // .andExpect(header().doesNotExist("Content-Type"));

        verifyNoInteractions(dashboardService);
    }

    @Test
    @DisplayName("GET /api/events/{id}/analytics?sessionId=12 → 200 (body ว่าง)")
    void analytics_with_session_ok() throws Exception {
        mockMvc.perform(get("/api/events/{eventId}/analytics", 123)
                        .param("sessionId", "12")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        // .andExpect(header().doesNotExist("Content-Type"));

        verifyNoInteractions(dashboardService);
    }

    @Test
    @DisplayName("POST /api/dashboard/summary → 405 Method Not Allowed")
    void summary_method_not_allowed() throws Exception {
        mockMvc.perform(post("/api/dashboard/summary"))
                .andExpect(status().isMethodNotAllowed());
    }

}


