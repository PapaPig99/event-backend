package com.example.eventproject.controller;

import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class EventControllerIT extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    void list_ok_print_shape() throws Exception {
        mvc.perform(get("/api/events"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getEvent_found_200() throws Exception {
        mvc.perform(get("/api/events/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists());
    }


}
