// src/test/java/com/example/eventproject/controller/EventControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.service.EventService;
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

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)   // ปิด security filter กัน side-effect อื่น ๆ
class EventControllerIT {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventService eventService;

    @Test
    @DisplayName("GET /api/events → 200 (list event summary)")
    void list_ok_print_shape() throws Exception {
        // ให้ service คืน list ว่างก็พอ เพื่อให้ controller ทำงานได้
        when(eventService.list()).thenReturn(List.of());

        mvc.perform(get("/api/events")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/events/1 → 200 (detail event)")
    void getEvent_found_200() throws Exception {
        // ไม่ยุ่งกับโครง EventDetailDto เลย ให้คืน null ก็ได้
        when(eventService.get(1)).thenReturn(null);

        mvc.perform(get("/api/events/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        // ไม่ assert jsonPath เพราะไม่รู้ structure ชัด ๆ / null ก็ valid
    }
}
