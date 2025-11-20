// src/test/java/com/example/eventproject/controller/SessionControllerIT.java
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

@WebMvcTest(controllers = SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerIT {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventSessionService service;

    @Test
    @DisplayName("GET /api/events/{eventId}/sessions → 200 + คืน list เป็น JSON")
    void listByEvent_ok_200() throws Exception {

        // สมมติให้ eventId = 1 มี session ว่าง ๆ (empty list)
        when(service.listByEvent(1)).thenReturn(List.of());

        mvc.perform(get("/api/events/1/sessions")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }
}
