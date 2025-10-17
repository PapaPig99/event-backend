package com.example.eventproject.controller;

import com.example.eventproject.dto.EventDetailDto;
import com.example.eventproject.dto.EventUpsertRequest;
import com.example.eventproject.service.EventService;
import com.example.eventproject.config.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private EventService eventService;

    // <<<<<< เพิ่มฟิลด์ไว้ใช้ข้ามเทส
    private Authentication auth;
    private CurrentUser principal;

    @BeforeEach
    void setUpAuth() {
        principal = Mockito.mock(CurrentUser.class);
        when(principal.getId()).thenReturn(42L);
        auth = new UsernamePasswordAuthenticationToken(
                principal, "N/A", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /* ===================== READ ===================== */

    @Test
    @DisplayName("GET /api/events → 200 และได้ลิสต์ (ว่าง)")
    void list_ok_empty() throws Exception {
        when(eventService.list()).thenReturn(List.of());

        mockMvc.perform(get("/api/events")
                        .with(authentication(auth))             // <- ต้องใส่ auth
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(eventService).list();
    }

    @Test
    @DisplayName("GET /api/events/{id} → 200 (serialize DTO ได้)")
    void get_ok() throws Exception {
        EventDetailDto detail = Mockito.mock(EventDetailDto.class);
        when(eventService.get(123)).thenReturn(detail);

        mockMvc.perform(get("/api/events/{id}", 123)
                        .with(authentication(auth))             // <- ต้องใส่ auth
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(eventService).get(123);
    }

    /* ===================== CREATE ===================== */

    @Test
    @DisplayName("POST /api/events (multipart) → 201 + Location")
    void create_ok_multipart() throws Exception {
        String jsonData = """
            { "title": "Dev Summit", "category": "Tech" }
            """;
        MockMultipartFile dataPart =
                new MockMultipartFile("data","data.json",MediaType.APPLICATION_JSON_VALUE,jsonData.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile poster =
                new MockMultipartFile("poster","poster.png","image/png",new byte[]{1,2});
        MockMultipartFile detail =
                new MockMultipartFile("detail","detail.pdf","application/pdf",new byte[]{3,4});
        MockMultipartFile seatmap =
                new MockMultipartFile("seatmap","seatmap.svg","image/svg+xml",new byte[]{5,6});

        when(eventService.create(any(EventUpsertRequest.class),
                any(MultipartFile.class), any(MultipartFile.class), any(MultipartFile.class), any(Integer.class)))
                .thenReturn(987);

        mockMvc.perform(multipart("/api/events")
                        .file(dataPart).file(poster).file(detail).file(seatmap)
                        .with(authentication(auth))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/events/987")));

        verify(eventService).create(any(EventUpsertRequest.class),
                any(MultipartFile.class), any(MultipartFile.class), any(MultipartFile.class), eq(42));
    }

    @Test
    @DisplayName("POST /api/events ขาด 'data' → 400")
    void create_missing_data_part_400() throws Exception {
        mockMvc.perform(multipart("/api/events")
                        .file(new MockMultipartFile("poster","poster.png","image/png",new byte[]{1}))
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    /* ===================== UPDATE ===================== */

    @Test
    @DisplayName("PUT /api/events/{id} (multipart) → 204")
    void update_ok_multipart() throws Exception {
        String jsonData = """
            { "title": "Renamed", "category": "Conference" }
            """;
        MockMultipartFile dataPart =
                new MockMultipartFile("data","data.json",MediaType.APPLICATION_JSON_VALUE,jsonData.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile poster =
                new MockMultipartFile("poster","poster.jpg","image/jpeg",new byte[]{7,8});

        doNothing().when(eventService).update(eq(555), any(EventUpsertRequest.class),
                any(MultipartFile.class), any(MultipartFile.class), any(MultipartFile.class));

        mockMvc.perform(multipart("/api/events/{id}", 555)
                        .file(dataPart).file(poster)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .with(authentication(auth))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(eventService).update(eq(555),
                any(EventUpsertRequest.class), any(MultipartFile.class), isNull(), isNull());
    }

    @Test
    @DisplayName("PUT /api/events/{id} ขาด 'data' → 400")
    void update_missing_data_part_400() throws Exception {
        mockMvc.perform(multipart("/api/events/{id}", 777)
                        .file(new MockMultipartFile("poster","poster.png","image/png",new byte[]{1}))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    /* ===================== DELETE ===================== */

    @Test
    @DisplayName("DELETE /api/events/{id} → 204")
    void delete_ok_204() throws Exception {
        doNothing().when(eventService).delete(321);

        mockMvc.perform(delete("/api/events/{id}", 321)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventService).delete(321);
    }

    /* ===================== 405 ตัวอย่าง ===================== */

    @Test
    @DisplayName("POST /api/events/{id} → 405 (ไม่มี mapping)")
    void post_to_get_endpoint_405() throws Exception {
        mockMvc.perform(post("/api/events/{id}", 1)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }
}
