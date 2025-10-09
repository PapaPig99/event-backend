package com.example.eventproject.service;

import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.repository.DashboardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ Unit Test ครอบคลุม DashboardService#getOverview()
 * - ทดสอบรวมยอด activeEvents / ticketsSold
 * - ทดสอบ map ข้อมูลจาก EventSalesRow → EventSalesSummary
 * - ทดสอบ null และ empty list
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository repo;

    @InjectMocks
    private DashboardService service;

    // ---------- Helper ----------
    private DashboardRepository.EventSalesRow row(Integer eventId, String title, String category,
                                                  Long capacity, Long sold) {
        var r = mock(DashboardRepository.EventSalesRow.class, Answers.RETURNS_DEEP_STUBS);
        when(r.getEventId()).thenReturn(eventId);
        when(r.getTitle()).thenReturn(title);
        when(r.getCategory()).thenReturn(category);
        when(r.getCapacity()).thenReturn(capacity);
        when(r.getSold()).thenReturn(sold);
        return r;
    }

    @Test
    @DisplayName("getOverview(): ปกติ → รวมยอดและ map ข้อมูลถูกต้อง")
    void getOverview_normal_case() {
        // Arrange
        when(repo.countActiveEvents()).thenReturn(5L);
        when(repo.sumTicketsSold()).thenReturn(100L);

        var r1 = row(1, "DevFest", "Tech", 500L, 200L);
        var r2 = row(2, "PyCon", "Programming", 300L, 150L);
        when(repo.findSalesRows()).thenReturn(List.of(r1, r2));

        // Act
        OverviewResponse out = service.getOverview();

        // Assert
        assertEquals(5L, out.getActiveEvents());
        assertEquals(100L, out.getTicketsSold());
        assertEquals(2, out.getSalesProgress().size());

        EventSalesSummary e1 = out.getSalesProgress().get(0);
        assertEquals(1, e1.getId());
        assertEquals("DevFest", e1.getTitle());
        assertEquals("Tech", e1.getCategory());
        assertEquals(500L, e1.getCapacity());
        assertEquals(200L, e1.getSold());

        EventSalesSummary e2 = out.getSalesProgress().get(1);
        assertEquals(2, e2.getId());
        assertEquals("PyCon", e2.getTitle());
        assertEquals("Programming", e2.getCategory());
        assertEquals(300L, e2.getCapacity());
        assertEquals(150L, e2.getSold());

        verify(repo).countActiveEvents();
        verify(repo).sumTicketsSold();
        verify(repo).findSalesRows();
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("getOverview(): capacity/sold == null → ต้องถูก map เป็น 0L")
    void getOverview_null_values() {
        when(repo.countActiveEvents()).thenReturn(2L);
        when(repo.sumTicketsSold()).thenReturn(20L);

        var r = row(9, "No Data", "Misc", null, null);
        when(repo.findSalesRows()).thenReturn(List.of(r));

        OverviewResponse out = service.getOverview();
        assertEquals(2L, out.getActiveEvents());
        assertEquals(20L, out.getTicketsSold());
        assertEquals(1, out.getSalesProgress().size());

        EventSalesSummary s = out.getSalesProgress().get(0);
        assertEquals(0L, s.getCapacity());
        assertEquals(0L, s.getSold());
    }

    @Test
    @DisplayName("getOverview(): repo.findSalesRows() ว่าง → คืน salesProgress=[]")
    void getOverview_empty_list() {
        when(repo.countActiveEvents()).thenReturn(1L);
        when(repo.sumTicketsSold()).thenReturn(5L);
        when(repo.findSalesRows()).thenReturn(List.of());

        OverviewResponse out = service.getOverview();

        assertEquals(1L, out.getActiveEvents());
        assertEquals(5L, out.getTicketsSold());
        assertNotNull(out.getSalesProgress());
        assertTrue(out.getSalesProgress().isEmpty());
    }
}
