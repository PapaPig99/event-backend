package com.example.eventproject.controller;

import com.example.eventproject.config.CurrentUser;
import com.example.eventproject.dto.EventDetailDto;
import com.example.eventproject.dto.EventDetailViewDto;
import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.dto.EventUpsertRequest;
import com.example.eventproject.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    EventService eventService;

    @InjectMocks
    EventController controller;

    /* ===================== READ ===================== */

    @Test
    @DisplayName("list() → เรียก service.list() และคืนลิสต์ได้")
    void list_ok_empty() {
        when(eventService.list()).thenReturn(List.of());

        List<EventSummaryView> result = controller.list();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventService).list();
    }

    @Test
    @DisplayName("get(id) → คืน DTO จาก service.get(id)")
    void get_ok() {
        EventDetailDto dto = mock(EventDetailDto.class);
        when(eventService.get(123)).thenReturn(dto);

        EventDetailDto result = controller.get(123);

        assertSame(dto, result);
        verify(eventService).get(123);
    }

    @Test
    @DisplayName("getView(id) → คืน DTO จาก service.getView(id)")
    void getView_ok() {
        EventDetailViewDto viewDto = mock(EventDetailViewDto.class);
        when(eventService.getView(10)).thenReturn(viewDto);

        EventDetailViewDto result = controller.getView(10);

        assertSame(viewDto, result);
        verify(eventService).getView(10);
    }

    /* ===================== CREATE ===================== */

    @Test
    @DisplayName("create(...) → 201 Created + Location ปลายทาง /api/events/{id}")
    void create_ok_multipart() {
        // เตรียม fake request context ให้ ServletUriComponentsBuilder ใช้
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/events");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // data (JSON) → ไม่ต้องสนใจเนื้อใน เพราะ controller แค่ส่งต่อไป service
        String jsonData = """
                { "title": "Dev Summit", "category": "Tech" }
                """;

        EventUpsertRequest dto = mock(EventUpsertRequest.class);

        MultipartFile poster = new MockMultipartFile(
                "poster",
                "poster.png",
                "image/png",
                new byte[]{1, 2}
        );

        MultipartFile seatmap = new MockMultipartFile(
                "seatmap",
                "seatmap.svg",
                "image/svg+xml",
                new byte[]{3, 4}
        );

        // mock CurrentUser
        CurrentUser user = mock(CurrentUser.class);
        when(user.getEmail()).thenReturn("user@example.com");

        when(eventService.create(dto, poster, seatmap, "user@example.com"))
                .thenReturn(987);

        ResponseEntity<Void> resp = controller.create(dto, poster, seatmap, user);

        assertEquals(201, resp.getStatusCodeValue());
        assertNotNull(resp.getHeaders().getLocation());
        assertTrue(resp.getHeaders().getLocation().toString().endsWith("/api/events/987"));

        verify(eventService).create(dto, poster, seatmap, "user@example.com");
    }

    /* ===================== UPDATE ===================== */

    @Test
    @DisplayName("update(...) → 204 No Content และเรียก service.update() ถูก")
    void update_ok_multipart() {
        EventUpsertRequest dto = mock(EventUpsertRequest.class);

        MultipartFile poster = new MockMultipartFile(
                "poster",
                "poster.jpg",
                "image/jpeg",
                new byte[]{7, 8}
        );

        // seatmap ไม่ส่ง → null
        doNothing().when(eventService).update(555, dto, poster, null);

        ResponseEntity<Void> resp = controller.update(555, dto, poster, null);

        assertEquals(204, resp.getStatusCodeValue());
        verify(eventService).update(555, dto, poster, null);
    }

    /* ===================== DELETE ===================== */

    @Test
    @DisplayName("delete(id) → 204 No Content และเรียก service.delete(id)")
    void delete_ok_204() {
        doNothing().when(eventService).delete(321);

        ResponseEntity<Void> resp = controller.delete(321);

        assertEquals(204, resp.getStatusCodeValue());
        verify(eventService).delete(321);
    }
}
