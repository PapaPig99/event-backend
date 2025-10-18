// src/test/java/com/example/eventproject/controller/DashboardControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.service.DashboardService;
import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @MockBean DashboardService service;

    @Test
    void summary_ok_200() throws Exception {
        var progress = List.of(
                new EventSalesSummary(1, "Concert", "Music", 500L, 300L)
        );
        var summary = new OverviewResponse(5, 1200, progress);
        when(service.getOverview()).thenReturn(summary);

        mvc.perform(get("/api/dashboard/summary")
                        .with(user("admin").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    var body = result.getResponse().getContentAsString();
                    if (body == null || body.isBlank()) {
                        throw new AssertionError("Expected non-empty JSON body");
                    }
                });
    }

    @Test
    void salesProgress_ok_200() throws Exception {
        var progress = List.of(
                new EventSalesSummary(1, "Music Fest", "Festival", 1000L, 850L)
        );
        var overview = new OverviewResponse(1, 850, progress);
        when(service.getOverview()).thenReturn(overview);

        mvc.perform(get("/api/dashboard/sales-progress")
                        .with(user("admin").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    var body = result.getResponse().getContentAsString();
                    if (body == null || body.isBlank()) {
                        throw new AssertionError("Expected non-empty JSON array");
                    }
                });
    }

    @Test
    void analytics_ok_200() throws Exception {
        mvc.perform(get("/api/events/1/analytics")
                        .with(user("admin").roles("ADMIN"))
                        .param("sessionId","10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}

