// src/test/java/com/example/eventproject/controller/AdminControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;

    @Test
    void dashboard_asAdmin_200() throws Exception {
        mvc.perform(get("/api/admin/dashboard")
                        // ใส่ผู้ใช้ ROLE_ADMIN เผื่อโปรเจกต์มีการบังคับสิทธิ์
                        .with(user("admin@test.com").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("admin only"));
    }
}
