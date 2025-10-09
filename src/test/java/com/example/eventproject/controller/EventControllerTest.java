package com.example.eventproject.controller;

import com.example.eventproject.dto.EventDetailDto;
import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.dto.EventUpsertRequest;
import com.example.eventproject.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Unit tests for EventController
 * - Focus: HTTP mapping/contract ของ Controller
 * - ใช้ @MockBean เพื่อควบคุมพฤติกรรมของ EventService
 * - ปิด Security filters เพื่อไม่ให้ 401/403 มารบกวน
 */
@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    /* ======================================================
     *                      READ
     * ====================================================== */

    @Test
    @DisplayName("GET /api/events → 200 และได้ลิสต์ (ทดสอบด้วยลิสต์ว่าง)")
    void list_ok_empty() throws Exception {
        when(eventService.list()).thenReturn(List.of());

        mockMvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(eventService, times(1)).list();
    }

    @Test
    @DisplayName("GET /api/events/{id} → 200 (serialize DTO ได้)")
    void get_ok() throws Exception {
        // ใช้ Mockito mock เพื่อไม่ผูกกับ structure ภายในของ DTO
        EventDetailDto detail = Mockito.mock(EventDetailDto.class);
        when(eventService.get(123)).thenReturn(detail);

        mockMvc.perform(get("/api/events/{id}", 123).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        // ไม่ assert field ภายใน เพื่อไม่ผูกกับ schema จริงเกินไป

        verify(eventService, times(1)).get(123);
    }

    /* ======================================================
     *                      CREATE
     * ====================================================== */

    @Test
    @DisplayName("POST /api/events (multipart/form-data) → 201 + Location header")
    void create_ok_multipart() throws Exception {
        // JSON ของพาร์ต 'data' — ปรับคีย์ตาม DTO จริงถ้าจำเป็น
        String jsonData = """
            { "title": "Dev Summit", "category": "Tech" }
            """;
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data.json", MediaType.APPLICATION_JSON_VALUE, jsonData.getBytes(StandardCharsets.UTF_8)
        );

        // ไฟล์รูป/รายละเอียด/seatmap (optional)
        MockMultipartFile poster = new MockMultipartFile("poster", "poster.png", "image/png", new byte[]{1,2});
        MockMultipartFile detail = new MockMultipartFile("detail", "detail.pdf", "application/pdf", new byte[]{3,4});
        MockMultipartFile seatmap = new MockMultipartFile("seatmap", "seatmap.svg", "image/svg+xml", new byte[]{5,6});

        when(eventService.create(any(EventUpsertRequest.class), any(), any(), any())).thenReturn(987);

        mockMvc.perform(multipart("/api/events")
                        .file(dataPart)
                        .file(poster)
                        .file(detail)
                        .file(seatmap)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/events/987")));

        verify(eventService, times(1)).create(any(EventUpsertRequest.class),
                any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/events ขาดพาร์ต 'data' → 400 Bad Request")
    void create_missing_data_part_400() throws Exception {
        // ไม่มีพาร์ต data → ควร 400
        mockMvc.perform(multipart("/api/events")
                        .file(new MockMultipartFile("poster", "poster.png", "image/png", new byte[]{1})))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    /* ======================================================
     *                      UPDATE
     * ====================================================== */

    @Test
    @DisplayName("PUT /api/events/{id} (multipart/form-data) → 204 No Content")
    void update_ok_multipart() throws Exception {
        String jsonData = """
            { "title": "Renamed", "category": "Conference" }
            """;
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data.json", MediaType.APPLICATION_JSON_VALUE, jsonData.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile poster = new MockMultipartFile("poster", "poster.jpg", "image/jpeg", new byte[]{7,8});

        doNothing().when(eventService).update(eq(555), any(EventUpsertRequest.class), any(), any(), any());

        // หมายเหตุ: multipart() ค่าเริ่มต้นเป็น POST → ต้องแปลงเป็น PUT
        mockMvc.perform(multipart("/api/events/{id}", 555)
                        .file(dataPart)
                        .file(poster)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).update(eq(555),
                any(EventUpsertRequest.class), any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/events/{id} ขาดพาร์ต 'data' → 400 Bad Request")
    void update_missing_data_part_400() throws Exception {
        mockMvc.perform(multipart("/api/events/{id}", 777)
                        .file(new MockMultipartFile("poster", "poster.png", "image/png", new byte[]{1}))
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    /* ======================================================
     *                      DELETE
     * ====================================================== */

    @Test
    @DisplayName("DELETE /api/events/{id} → 204 No Content")
    void delete_ok_204() throws Exception {
        doNothing().when(eventService).delete(321);

        mockMvc.perform(delete("/api/events/{id}", 321))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).delete(321);
    }

    /* ======================================================
     *                METHOD NOT ALLOWED (ตัวอย่าง)
     * ====================================================== */

    @Test
    @DisplayName("POST /api/events/{id} → 405 Method Not Allowed (ไม่มี mapping)")
    void post_to_get_endpoint_405() throws Exception {
        mockMvc.perform(post("/api/events/{id}", 1))
                .andExpect(status().isMethodNotAllowed());
    }
}
