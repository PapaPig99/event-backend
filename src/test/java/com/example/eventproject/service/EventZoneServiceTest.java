package com.example.eventproject.service;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.repository.EventZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventZoneService — pure Mockito (no Spring)
 */
@ExtendWith(MockitoExtension.class)
class EventZoneServiceTest {

    @Mock
    EventZoneRepository zoneRepository;

    @InjectMocks
    EventZoneService service;

    @Test
    @DisplayName("getAvailabilityBySession: คืน DTO list ตามที่ repo ส่งมา และส่ง sessionId ให้ repo ตรง")
    void getAvailability_returnsList_andCallsRepoWithCorrectParam() {
        Integer sessionId = 10;

        List<ZoneAvailabilityDto> stub =
                List.of(
                        new ZoneAvailabilityDto(1, "A", 100, 40L, 60L),
                        new ZoneAvailabilityDto(2, "B", 80, 80L, 0L)
                );

        when(zoneRepository.findAvailabilityBySession(eq(sessionId))).thenReturn(stub);

        List<ZoneAvailabilityDto> result = service.getAvailabilityBySession(sessionId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).zoneId()).isEqualTo(1);
        assertThat(result.get(0).zoneName()).isEqualTo("A");
        assertThat(result.get(0).capacity()).isEqualTo(100);
        assertThat(result.get(0).booked()).isEqualTo(40L);
        assertThat(result.get(0).available()).isEqualTo(60L);

        assertThat(result.get(1).zoneId()).isEqualTo(2);
        assertThat(result.get(1).zoneName()).isEqualTo("B");
        assertThat(result.get(1).capacity()).isEqualTo(80);
        assertThat(result.get(1).booked()).isEqualTo(80L);
        assertThat(result.get(1).available()).isEqualTo(0L);

        verify(zoneRepository, times(1)).findAvailabilityBySession(eq(sessionId));
        verifyNoMoreInteractions(zoneRepository);
    }

    @Test
    @DisplayName("getAvailabilityBySession: เมื่อ repo คืนลิสต์ว่าง ควรได้ลิสต์ว่างกลับมา")
    void getAvailability_emptyList() {
        Integer sessionId = 99;
        when(zoneRepository.findAvailabilityBySession(eq(sessionId))).thenReturn(List.of());

        List<ZoneAvailabilityDto> result = service.getAvailabilityBySession(sessionId);

        assertThat(result).isEmpty();
        verify(zoneRepository).findAvailabilityBySession(eq(sessionId));
        verifyNoMoreInteractions(zoneRepository);
    }

    @Test
    @DisplayName("getAvailabilityBySession: ส่งต่อ exception จาก repo (ไม่กลืนทิ้ง)")
    void getAvailability_propagatesException() {
        Integer sessionId = 7;
        RuntimeException boom = new RuntimeException("boom");
        when(zoneRepository.findAvailabilityBySession(eq(sessionId))).thenThrow(boom);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getAvailabilityBySession(sessionId));

        assertThat(ex).isSameAs(boom);
        verify(zoneRepository).findAvailabilityBySession(eq(sessionId));
        verifyNoMoreInteractions(zoneRepository);
    }

    @Test
    @DisplayName("getAvailabilityBySession: อนุญาตให้ sessionId เป็น null และส่งต่อให้ repo ตามเดิม (ขึ้นกับชั้น repo จะ validate เอง)")
    void getAvailability_allowsNullAndDelegates() {
        when(zoneRepository.findAvailabilityBySession(null)).thenReturn(List.of());

        List<ZoneAvailabilityDto> result = service.getAvailabilityBySession(null);

        assertThat(result).isEmpty();
        // ตรวจลำดับการเรียก (แค่ตัวอย่าง ใช้ InOrder ได้ถ้ามีหลายเมธอด)
        InOrder inOrder = Mockito.inOrder(zoneRepository);
        inOrder.verify(zoneRepository).findAvailabilityBySession(null);
        verifyNoMoreInteractions(zoneRepository);
    }
}
