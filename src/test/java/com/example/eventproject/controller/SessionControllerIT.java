// src/test/java/com/example/eventproject/controller/SessionControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;

    @Test
    void listByEvent_ok_200() throws Exception {
        mvc.perform(get("/api/events/1/sessions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("")); // controller คืน ok().build()
    }

    @Test
    void create_ok_201() throws Exception {
        mvc.perform(post("/api/events/1/sessions")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))   // 👈 อัดผู้ใช้
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void update_ok_200() throws Exception {
        mvc.perform(put("/api/sessions/123")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))   // 👈 อัดผู้ใช้
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void delete_noContent_204() throws Exception {
        mvc.perform(delete("/api/sessions/123")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))  // 👈 อัดผู้ใช้
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
