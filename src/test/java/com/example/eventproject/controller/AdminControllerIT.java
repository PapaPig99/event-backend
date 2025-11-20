// src/test/java/com/example/eventproject/controller/AdminControllerTest.java
package com.example.eventproject.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // ปิด Security filter เวลาเทส
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /api/admin/dashboard → 200 + JSON {ok:true, msg:'admin only'}")
    void dashboard_asAdmin_200() throws Exception {
        mvc.perform(get("/api/admin/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("admin only"));
    }
}
