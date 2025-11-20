package com.example.eventproject.controller;

import com.example.eventproject.service.EventSessionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Tests for SessionController (เฉพาะ GET /api/events/{eventId}/sessions)
 */
@WebMvcTest(controllers = SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventSessionService eventSessionService;

    @Test
    @DisplayName("GET /api/events/{eventId}/sessions → 200 + [] (empty list)")
    void listByEvent_ok_emptyList() throws Exception {
        // arrange
        when(eventSessionService.listByEvent(10)).thenReturn(List.of());

        // act + assert
        mockMvc.perform(get("/api/events/{eventId}/sessions", 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }
}
