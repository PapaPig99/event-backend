// src/test/java/com/example/eventproject/service/DashboardServiceTest.java
package com.example.eventproject.service;

import com.example.eventproject.dto.DashboardDto;
import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.model.Event;
import com.example.eventproject.repository.DashboardRepository;
import com.example.eventproject.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    DashboardRepository repo;

    @Mock
    EventRepository eventRepo;

    @InjectMocks
    DashboardService service;

    // ========================================================================
    // getDashboard(null)  — สรุปทั้งหมด
    // ========================================================================

    @Test
    @DisplayName("getDashboard(null): คำนวณสรุปทุก Event ถูกต้อง")
    void getDashboard_allEvents() {
        when(repo.countActiveEvents()).thenReturn(3L);
        when(repo.countTicketsSoldAll()).thenReturn(80L);
        when(repo.countRegistrationsAll()).thenReturn(100L);
        when(repo.countCheckinAll()).thenReturn(60L);

        DashboardDto dto = service.getDashboard(null);

        assertNotNull(dto);

        assertEquals(3L, dto.activeEvents());
        assertEquals(80L, dto.ticketsSold());
        assertEquals(100L, dto.totalRegistrations());
        assertEquals(100L, dto.totalSignups());
        assertEquals(20L, dto.dropOffs());
        assertEquals(75L, dto.showRate());
        assertEquals(60L, dto.checkIn());

        verify(repo).countActiveEvents();
        verify(repo).countTicketsSoldAll();
        verify(repo).countRegistrationsAll();
        verify(repo).countCheckinAll();
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventRepo);
    }

    // ========================================================================
    // getDashboard(eventId)  — เฉพาะ event
    // ========================================================================

    @Test
    @DisplayName("getDashboard(eventId): คำนวณสรุปเฉพาะ Event ถูกต้อง")
    void getDashboard_specificEvent() {
        Integer eventId = 10;

        when(repo.countSignupsByEvent(eventId)).thenReturn(50L);
        when(repo.countDropoffsByEvent(eventId)).thenReturn(10L);
        when(repo.countTicketsSoldByEvent(eventId)).thenReturn(40L);
        when(repo.countCheckinByEvent(eventId)).thenReturn(30L);

        DashboardDto dto = service.getDashboard(eventId);

        assertNotNull(dto);

        assertEquals(0L, dto.activeEvents());
        assertEquals(40L, dto.ticketsSold());
        assertEquals(50L, dto.totalRegistrations());
        assertEquals(50L, dto.totalSignups());
        assertEquals(10L, dto.dropOffs());
        assertEquals(75L, dto.showRate());
        assertEquals(30L, dto.checkIn());

        verify(repo).countSignupsByEvent(eventId);
        verify(repo).countDropoffsByEvent(eventId);
        verify(repo).countTicketsSoldByEvent(eventId);
        verify(repo).countCheckinByEvent(eventId);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventRepo);
    }

    // ========================================================================
    // getEventTable()
    // ========================================================================

    @Test
    @DisplayName("getEventTable(): map Event → EventSalesSummary ถูกต้อง")
    void getEventTable_ok() {

        Event e1 = new Event();
        e1.setId(1);
        e1.setTitle("DevConf");
        e1.setCategory("Tech");

        Event e2 = new Event();
        e2.setId(2);
        e2.setTitle("Music Fest");
        e2.setCategory("Festival");

        // ให้ repo คืน list ของ event 2 ตัวนี้
        when(eventRepo.findAll()).thenReturn(List.of(e1, e2));

        // 👉 ผูก stub ตาม eventId จริง ๆ (หลีกเลี่ยง null + NPE จาก unboxing)
        when(repo.sumCapacityByEvent(1)).thenReturn(1000L);
        when(repo.sumCapacityByEvent(2)).thenReturn(500L);

        when(repo.countSoldByEvent(1)).thenReturn(700L);
        when(repo.countSoldByEvent(2)).thenReturn(250L);

        List<EventSalesSummary> result = service.getEventTable();

        assertNotNull(result);
        assertEquals(2, result.size());

        EventSalesSummary s1 = result.get(0);
        assertEquals(1, s1.eventId());
        assertEquals("DevConf", s1.title());
        assertEquals("Tech", s1.category());
        assertEquals(1000L, s1.capacity());
        assertEquals(700L, s1.sold());

        EventSalesSummary s2 = result.get(1);
        assertEquals(2, s2.eventId());
        assertEquals("Music Fest", s2.title());
        assertEquals("Festival", s2.category());
        assertEquals(500L, s2.capacity());
        assertEquals(250L, s2.sold());

        verify(eventRepo).findAll();
        verify(repo).sumCapacityByEvent(1);
        verify(repo).sumCapacityByEvent(2);
        verify(repo).countSoldByEvent(1);
        verify(repo).countSoldByEvent(2);
        verifyNoMoreInteractions(repo, eventRepo);
    }
}
