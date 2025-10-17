package com.example.eventproject.controller;

import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;  // ✅ เพิ่มบรรทัดนี้
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;

    @Test
    void getEvent_found_200() throws Exception {
        mvc.perform(get("/api/events/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("MARIAH CAREY The Celebration of Mimi")); // ✅ ให้ตรง data.sql
    }

    @Test
    void getEvent_notFound_404() throws Exception {
        mvc.perform(get("/api/events/999999"))
                .andExpect(status().isNotFound());
    }
}
