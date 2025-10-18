// src/test/java/com/example/eventproject/controller/ZoneControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.service.EventZoneService;
import com.example.eventproject.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZoneControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;

    @MockBean EventZoneService zoneService;

    @Test
    void getAvailability_ok_200_withOneItem() throws Exception {
        // ใช้ Mockito mock DTO เพื่อไม่ต้องรู้คอนสตรัคเตอร์
        ZoneAvailabilityDto item = mock(ZoneAvailabilityDto.class);
        when(zoneService.getAvailabilityBySession(101))
                .thenReturn(List.of(item));

        mvc.perform(get("/api/zones/session/101/availability"))
                .andDo(print())
                .andExpect(status().isOk())
                // เช็คว่าเป็น array และมี 1 รายการ
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAvailability_empty_ok_200() throws Exception {
        when(zoneService.getAvailabilityBySession(999))
                .thenReturn(List.of());

        mvc.perform(get("/api/zones/session/999/availability"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
