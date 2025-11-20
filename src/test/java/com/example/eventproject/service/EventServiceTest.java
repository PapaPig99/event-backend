package com.example.eventproject.service;

import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.dto.EventUpsertRequest;
import com.example.eventproject.model.Event;
import com.example.eventproject.repository.EventRepository;
import com.example.eventproject.repository.EventSessionRepository;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.RegistrationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService (pure Mockito, no Spring context).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // 👈 สำคัญ: ผ่อนคลายเรื่อง unused stubbing
class EventServiceTest {

    @Mock private EventRepository eventRepo;
    @Mock private EventSessionRepository sessionRepo;
    @Mock private EventZoneRepository zoneRepo;
    @Mock private FileStorageService fileStorageService;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private ZoneTemplateService zoneTemplateService;

    @InjectMocks private EventService service;

    @Captor private ArgumentCaptor<Event> eventCaptor;

    /* =============== helpers =============== */

    /** ไฟล์ non-empty สำหรับใช้ทดสอบเคสอัปโหลด */
    private MultipartFile nonEmptyFile() {
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        return f;
    }

    /** ไฟล์ empty (isEmpty=true) ใช้จำลอง "ไม่มีอัปโหลด" */
    private MultipartFile emptyFile() {
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(true);
        return f;
    }

    /** mock EventUpsertRequest เฉพาะ field ที่ EventService ใช้แน่ ๆ */
    private EventUpsertRequest mockDto(String posterUrl, String seatmapUrl) {
        EventUpsertRequest dto = mock(EventUpsertRequest.class, Answers.RETURNS_DEEP_STUBS);
        // core fields
        when(dto.title()).thenReturn("Test Event");
        when(dto.category()).thenReturn("CAT");
        when(dto.location()).thenReturn("LOC");

        // image url จาก dto (ใช้ใน setImagesFromUploadsOrDto)
        when(dto.posterImageUrl()).thenReturn(posterUrl);
        when(dto.seatmapImageUrl()).thenReturn(seatmapUrl);

        // ไม่ให้มี sessions เพื่อเลี่ยง logic ซับซ้อน (template / zones)
        when(dto.sessions()).thenReturn(null);

        return dto;
    }

    /* =============== list() =============== */

    @Test
    @DisplayName("list(): ดึงรายการจาก repo ตรง ๆ")
    void list_ok() {
        List<EventSummaryView> fake = List.of(
                mock(EventSummaryView.class),
                mock(EventSummaryView.class)
        );
        when(eventRepo.findAllByOrderByStartDateAsc()).thenReturn(fake);

        List<EventSummaryView> out = service.list();

        assertSame(fake, out);
        verify(eventRepo).findAllByOrderByStartDateAsc();
        verifyNoMoreInteractions(eventRepo);
        verifyNoInteractions(sessionRepo, zoneRepo, fileStorageService, registrationRepository, zoneTemplateService);
    }

    /* =============== create(...) =============== */

    @Test
    @DisplayName("create: มีไฟล์อัปโหลด poster/seatmap → ใช้ replaceFile แล้ว save event พร้อม url ใหม่")
    void create_with_uploads() {
        EventUpsertRequest dto = mockDto(null, null);

        MultipartFile poster  = nonEmptyFile();
        MultipartFile seatmap = nonEmptyFile();

        when(fileStorageService.replaceFile(isNull(), eq(poster))).thenReturn("poster-new");
        when(fileStorageService.replaceFile(isNull(), eq(seatmap))).thenReturn("seatmap-new");

        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(123);
            return e;
        });

        Integer id = service.create(dto, poster, seatmap, "owner@example.com");
        assertEquals(123, id);

        verify(eventRepo, atLeastOnce()).save(eventCaptor.capture());
        Event saved = eventCaptor.getValue();
        assertEquals("Test Event", saved.getTitle());
        assertEquals("poster-new", saved.getPosterImageUrl());
        assertEquals("seatmap-new", saved.getSeatmapImageUrl());

        // ไม่มีการลบไฟล์ใน create
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    @DisplayName("create: ไม่มีไฟล์ แต่ dto ส่ง URL → ใช้ URL จาก dto และไม่ยุ่ง fileStorage")
    void create_without_uploads_but_urls_from_dto() {
        EventUpsertRequest dto = mockDto("posterDTO", "seatDTO");

        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(9);
            return e;
        });

        Integer id = service.create(dto, emptyFile(), emptyFile(), "owner@example.com");
        assertEquals(9, id);

        verify(eventRepo, atLeastOnce()).save(eventCaptor.capture());
        Event saved = eventCaptor.getValue();
        assertEquals("posterDTO", saved.getPosterImageUrl());
        assertEquals("seatDTO", saved.getSeatmapImageUrl());

        verify(fileStorageService, never()).replaceFile(any(), any());
        verify(fileStorageService, never()).deleteFile(any());
    }

    /* =============== update(...) =============== */

    @Test
    @DisplayName("update: อัปโหลดไฟล์ใหม่ → replaceFile จาก url เก่า และ save event")
    void update_with_uploads() {
        Event existing = new Event();
        existing.setId(77);
        existing.setPosterImageUrl("old-poster");
        existing.setSeatmapImageUrl("old-seat");

        when(eventRepo.findById(77)).thenReturn(Optional.of(existing));
        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile poster  = nonEmptyFile();
        MultipartFile seatmap = nonEmptyFile();

        when(fileStorageService.replaceFile(eq("old-poster"), eq(poster))).thenReturn("poster-new");
        when(fileStorageService.replaceFile(eq("old-seat"), eq(seatmap))).thenReturn("seat-new");

        EventUpsertRequest dto = mockDto(null, null); // dto ไม่ส่ง url → ให้ใช้ไฟล์ใหม่แทน

        service.update(77, dto, poster, seatmap);

        assertEquals("poster-new", existing.getPosterImageUrl());
        assertEquals("seat-new", existing.getSeatmapImageUrl());

        verify(eventRepo, atLeastOnce()).save(existing);
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    @DisplayName("update: ไม่อัปโหลด + dto.url = null → deleteFile url เดิม และ set เป็น null")
    void update_clear_images_when_dto_urls_null() {
        Event existing = new Event();
        existing.setId(55);
        existing.setPosterImageUrl("old-p");
        existing.setSeatmapImageUrl("old-s");

        when(eventRepo.findById(55)).thenReturn(Optional.of(existing));
        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventUpsertRequest dto = mockDto(null, null); // ทั้ง posterImageUrl / seatmapImageUrl = null

        service.update(55, dto, emptyFile(), emptyFile());

        verify(fileStorageService).deleteFile("old-p");
        verify(fileStorageService).deleteFile("old-s");
        assertNull(existing.getPosterImageUrl());
        assertNull(existing.getSeatmapImageUrl());

        verify(eventRepo, atLeastOnce()).save(existing);
    }

    @Test
    @DisplayName("update: หา event ไม่เจอ → IllegalArgumentException")
    void update_notFound() {
        when(eventRepo.findById(999)).thenReturn(Optional.empty());

        // ส่ง null สำหรับไฟล์ เพื่อไม่สร้าง stubbing เพิ่ม
        assertThrows(IllegalArgumentException.class,
                () -> service.update(999, mockDto(null, null), null, null));

        verify(eventRepo).findById(999);
        verifyNoMoreInteractions(eventRepo);
        verifyNoInteractions(sessionRepo, zoneRepo, fileStorageService, registrationRepository, zoneTemplateService);
    }

    /* =============== delete(id) =============== */

    @Test
    @DisplayName("delete: ลบไฟล์ poster/seatmap + ลบ regis + sessions + event")
    void delete_ok() {
        Event e = new Event();
        e.setId(101);
        e.setPosterImageUrl("p");
        e.setSeatmapImageUrl("s");

        when(eventRepo.findById(101)).thenReturn(Optional.of(e));

        service.delete(101);

        verify(fileStorageService).deleteFile("p");
        verify(fileStorageService).deleteFile("s");
        verify(registrationRepository).deleteAllByEventCascade(101);
        verify(sessionRepo).deleteByEvent_Id(101);
        verify(eventRepo).delete(e);
    }

    @Test
    @DisplayName("delete: ไม่พบ event → IllegalArgumentException")
    void delete_notFound() {
        when(eventRepo.findById(404)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.delete(404));

        verify(eventRepo).findById(404);
        verifyNoMoreInteractions(eventRepo);
        verifyNoInteractions(sessionRepo, zoneRepo, fileStorageService, registrationRepository, zoneTemplateService);
    }
}
