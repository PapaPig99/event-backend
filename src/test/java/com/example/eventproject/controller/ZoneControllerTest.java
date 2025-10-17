package com.example.eventproject.controller;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.service.EventZoneService;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * Controller-only tests for ZoneController:
 * - ปิด security filters
 * - mock EventZoneService
 */
@WebMvcTest(controllers = ZoneController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ZoneControllerTest.TestErrorHandler.class)
class ZoneControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventZoneService zoneService;

    @Test
    @DisplayName("GET /api/zones/session/{sessionId}/availability :: 200 + คืนรายการพร้อมฟิลด์ครบ และเรียก service ด้วย sessionId ที่ถูกต้อง")
    void getAvailability_shouldReturn200_andCallService() throws Exception {
        Integer sessionId = 10;

        // ใช้ DTO จริงตาม signature ของ service
        List<ZoneAvailabilityDto> payload = List.of(
                new ZoneAvailabilityDto(1, "A", 100, 40L, 60L),
                new ZoneAvailabilityDto(2, "B",  80, 80L,  0L)
        );
        Mockito.when(zoneService.getAvailabilityBySession(eq(sessionId))).thenReturn(payload);

        mvc.perform(get("/api/zones/session/{sessionId}/availability", sessionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                // item #0
                .andExpect(jsonPath("$[0].zoneId",       equalTo(1)))
                .andExpect(jsonPath("$[0].zoneName",     equalTo("A")))
                .andExpect(jsonPath("$[0].capacity",     equalTo(100)))
                .andExpect(jsonPath("$[0].booked",       equalTo(40)))
                .andExpect(jsonPath("$[0].available",    equalTo(60)))
                // item #1
                .andExpect(jsonPath("$[1].zoneId",       equalTo(2)))
                .andExpect(jsonPath("$[1].zoneName",     equalTo("B")))
                .andExpect(jsonPath("$[1].capacity",     equalTo(80)))
                .andExpect(jsonPath("$[1].booked",       equalTo(80)))
                .andExpect(jsonPath("$[1].available",    equalTo(0)));

        verify(zoneService, times(1)).getAvailabilityBySession(eq(sessionId));
    }

    @Test
    @DisplayName("GET /api/zones/session/{sessionId}/availability :: 200 (empty list)")
    void getAvailability_shouldReturnEmptyList() throws Exception {
        Integer sessionId = 99;
        Mockito.when(zoneService.getAvailabilityBySession(eq(sessionId))).thenReturn(List.of());

        mvc.perform(get("/api/zones/session/{sessionId}/availability", sessionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(zoneService, times(1)).getAvailabilityBySession(eq(sessionId));
    }

    @RestControllerAdvice
    static class TestErrorHandler {
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handle(Exception ex) {
            return ResponseEntity.status(500)
                    .body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @Test
    @DisplayName("GET /api/zones/session/{sessionId}/availability :: 5xx เมื่อ service โยน exception")
    void getAvailability_whenServiceThrows_then5xx() throws Exception {
        Integer sessionId = 7;
        Mockito.when(zoneService.getAvailabilityBySession(eq(sessionId)))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/api/zones/session/{sessionId}/availability", sessionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(zoneService, times(1)).getAvailabilityBySession(eq(sessionId));
    }

}
