// src/test/java/com/example/eventproject/controller/ZoneControllerIT.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ZoneController.class)
@AutoConfigureMockMvc(addFilters = false)
class ZoneControllerIT {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventZoneService eventZoneService;

    @Test
    @DisplayName("GET /api/zones/session/{sessionId}/availability → 200 + array length = 1")
    void getAvailability_ok_200_withOneItem() throws Exception {
        // mock DTO ขึ้นมาหนึ่งตัว (ไม่ต้องสน field ข้างใน)
        ZoneAvailabilityDto item = Mockito.mock(ZoneAvailabilityDto.class);

        when(eventZoneService.getAvailabilityBySession(101))
                .thenReturn(List.of(item));

        mvc.perform(get("/api/zones/session/{sessionId}/availability", 101)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // แค่เช็คว่าเป็น array และมี 1 element
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/zones/session/{sessionId}/availability → 200 + [] (empty)")
    void getAvailability_empty_ok_200() throws Exception {
        when(eventZoneService.getAvailabilityBySession(999))
                .thenReturn(List.of());

        mvc.perform(get("/api/zones/session/{sessionId}/availability", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/zones/group?sessionId=&groupName= → 200 + [] (mock empty)")
    void getZonesByGroup_empty_ok_200() throws Exception {
        when(eventZoneService.getZonesByGroup(1, "VIP"))
                .thenReturn(List.of());

        mvc.perform(get("/api/zones/group")
                        .param("sessionId", "1")
                        .param("groupName", "VIP")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/zones/group → 200 + has 1 zone")
    void getZonesByGroup_oneItem_ok_200() throws Exception {
        EventZone zone = new EventZone();
        zone.setId(10);
        zone.setName("VIP");

        when(eventZoneService.getZonesByGroup(2, "VIP"))
                .thenReturn(List.of(zone));

        mvc.perform(get("/api/zones/group")
                        .param("sessionId", "2")
                        .param("groupName", "VIP")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("VIP"));
    }
}
