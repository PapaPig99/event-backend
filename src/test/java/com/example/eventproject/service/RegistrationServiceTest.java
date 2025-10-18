package com.example.eventproject.service;


import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.model.Event;
import com.example.eventproject.model.EventSession;
import com.example.eventproject.model.Registration;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.RegistrationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock RegistrationRepository registrationRepository;
    @Mock EventZoneRepository eventZoneRepository;

    @InjectMocks RegistrationService service;

    /* ---------- helpers ---------- */

    private RegistrationDto.CreateRequest createReq(Integer eventId, Integer sessionId, Integer zoneId, Integer qty) {
        return new RegistrationDto.CreateRequest(eventId, sessionId, zoneId, qty);
    }

    private Registration newReg(Integer id, Integer userId, Integer eventId, Integer sessionId, Registration.RegStatus rs, Registration.PayStatus ps) {
        Registration r = new Registration();
        r.setId(id);
        r.setUserId(userId);
        r.setEvent(new Event(eventId));
        r.setSession(new EventSession(sessionId));
        r.setRegistrationStatus(rs);
        r.setPaymentStatus(ps);
        return r;
    }

    private RegistrationDto.Response respOf(Registration reg) {
        return new RegistrationDto.Response(
                reg.getId(),                                              // id
                reg.getUserId(),                                          // userId
                reg.getEvent()   != null ? reg.getEvent().getId()   : null, // eventId
                reg.getSession() != null ? reg.getSession().getId() : null, // sessionId
                reg.getZone()    != null ? reg.getZone().getId()    : null, // zoneId

                reg.getRegistrationStatus() != null ? reg.getRegistrationStatus().name() : null, // registrationStatus (String)
                reg.getQuantity(),                                         // quantity (Integer)
                reg.getPaymentStatus() != null ? reg.getPaymentStatus().name() : null,          // paymentStatus (String)
                null,                                                      // paymentMethod / channel (ไม่มีใน model -> ใส่ null)

                reg.getUnitPrice(),                                        // pricePerTicket
                reg.getTotalPrice(),                                       // totalPrice

                reg.getHoldExpiresAt(),                                    // reservedUntil
                reg.getRegisteredAt(),                                     // createdAt
                reg.getUpdatedAt(),                                        // updatedAt
                reg.getPaidAt(),                                           // paidAt

                reg.getPaymentReference(),                                 // txRef (String)
                reg.getCancelledReason() != null ? reg.getCancelledReason().name() : null, // cancelReason (String)
                null                                                       // user (UserDto) – ไม่ใช้ในเทสนี้
        );
    }


    /* ===================== create ===================== */

    @Test
    @DisplayName("create: happy path — คำนวณราคา/ตั้งสถานะ/เวลาถูกต้อง และ save() ด้วย reg ที่ประกอบจาก req")
    void create_success() {
        var req = createReq(1, 10, 5, 3);
        Integer userId = 123;

        // zone ที่พบ + ราคา
        var zone = new com.example.eventproject.model.EventZone();
        zone.setId(5);
        zone.setPrice(new BigDecimal("250.00"));
        when(eventZoneRepository.findById(eq(5))).thenReturn(Optional.of(zone));

        // ไม่มี conflict
        when(registrationRepository.findActiveOrConfirmedForUserAndSession(eq(userId), eq(10), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // stub save -> คืน object เดิมโดย set id
        Answer<Registration> saveAnswer = inv -> {
            Registration r = inv.getArgument(0);
            r.setId(999);
            return r;
        };
        when(registrationRepository.save(any(Registration.class))).thenAnswer(saveAnswer);

        Registration saved = service.create(req, userId);

        assertThat(saved.getId()).isEqualTo(999);
        assertThat(saved.getUserId()).isEqualTo(123);
        assertThat(saved.getEvent().getId()).isEqualTo(1);
        assertThat(saved.getSession().getId()).isEqualTo(10);
        assertThat(saved.getZone().getId()).isEqualTo(5);
        assertThat(saved.getQuantity()).isEqualTo(3);
        assertThat(saved.getRegistrationStatus()).isEqualTo(Registration.RegStatus.PENDING);
        assertThat(saved.getPaymentStatus()).isEqualTo(Registration.PayStatus.UNPAID);
        assertThat(saved.getUnitPrice()).isEqualByComparingTo("250.00");
        assertThat(saved.getTotalPrice()).isEqualByComparingTo("750.00");
        assertThat(saved.getRegisteredAt()).isNotNull();
        assertThat(saved.getHoldExpiresAt()).isNotNull();
        // hold = 10 นาทีหลัง registered
        assertThat(Duration.between(saved.getRegisteredAt(), saved.getHoldExpiresAt()).toMinutes()).isBetween(9L, 11L);

        verify(eventZoneRepository).findById(5);
        verify(registrationRepository).findActiveOrConfirmedForUserAndSession(eq(userId), eq(10), any(LocalDateTime.class));
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @DisplayName("create: quantity <= 0 -> IllegalArgumentException")
    void create_invalidQuantity() {
        var bad = createReq(1, 10, 5, 0);
        assertThatThrownBy(() -> service.create(bad, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
        verifyNoInteractions(eventZoneRepository, registrationRepository);
    }

    @Test
    @DisplayName("create: zone ไม่พบ -> IllegalArgumentException('zone not found')")
    void create_zoneNotFound() {
        when(eventZoneRepository.findById(eq(5))).thenReturn(Optional.empty());
        var req = createReq(1, 10, 5, 1);
        assertThatThrownBy(() -> service.create(req, 123))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("zone not found");
        verify(eventZoneRepository).findById(5);
        verifyNoMoreInteractions(eventZoneRepository);
        verifyNoInteractions(registrationRepository);
    }

    @Test
    @DisplayName("create: มี conflict booking เดิมใน session เดียวกัน -> IllegalStateException")
    void create_conflict() {
        var req = createReq(1, 10, 5, 1);
        var zone = new com.example.eventproject.model.EventZone();
        zone.setId(5);
        zone.setPrice(new BigDecimal("100.00"));
        when(eventZoneRepository.findById(5)).thenReturn(Optional.of(zone));

        Registration existing = newReg(321, 123, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        when(registrationRepository.findActiveOrConfirmedForUserAndSession(eq(123), eq(10), any(LocalDateTime.class)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.create(req, 123))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already have a booking");

        verify(eventZoneRepository).findById(5);
        verify(registrationRepository).findActiveOrConfirmedForUserAndSession(eq(123), eq(10), any(LocalDateTime.class));
        verify(registrationRepository, never()).save(any());
    }

    /* ===================== confirm ===================== */

    @Test
    @DisplayName("confirm: happy path — เปลี่ยนสถานะเป็น CONFIRMED/PAID เซ็ต reference, paidAt, updatedAt แล้ว save")
    void confirm_success() {
        Integer id = 77;
        Integer userId = 123;
        var req = new RegistrationDto.ConfirmRequest("TX-OK-777");

        Registration reg = newReg(id, userId, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        reg.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(registrationRepository.findById(id)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration saved = service.confirm(id, req, userId);

        assertThat(saved.getRegistrationStatus()).isEqualTo(Registration.RegStatus.CONFIRMED);
        assertThat(saved.getPaymentStatus()).isEqualTo(Registration.PayStatus.PAID);
        assertThat(saved.getPaymentReference()).isEqualTo("TX-OK-777");
        assertThat(saved.getPaidAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        verify(registrationRepository).findById(id);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @DisplayName("confirm: reg ไม่พบ -> IllegalArgumentException")
    void confirm_notFound() {
        when(registrationRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.confirm(1, new RegistrationDto.ConfirmRequest("x"), 123))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registration not found");
    }

    @Test
    @DisplayName("confirm: ไม่ใช่เจ้าของ -> SecurityException('forbidden')")
    void confirm_forbidden() {
        Registration reg = newReg(1, 999, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(reg));
        assertThatThrownBy(() -> service.confirm(1, new RegistrationDto.ConfirmRequest("x"), 123))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("forbidden");
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirm: สถานะไม่ใช่ PENDING -> IllegalStateException('not PENDING')")
    void confirm_notPending() {
        Registration reg = newReg(1, 123, 1, 10, Registration.RegStatus.CONFIRMED, Registration.PayStatus.PAID);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(reg));
        assertThatThrownBy(() -> service.confirm(1, new RegistrationDto.ConfirmRequest("x"), 123))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not PENDING");
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirm: hold หมดอายุ -> IllegalStateException('hold expired')")
    void confirm_holdExpired() {
        Registration reg = newReg(1, 123, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        reg.setHoldExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(registrationRepository.findById(1)).thenReturn(Optional.of(reg));
        assertThatThrownBy(() -> service.confirm(1, new RegistrationDto.ConfirmRequest("x"), 123))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hold expired");
        verify(registrationRepository, never()).save(any());
    }

    /* ===================== cancel ===================== */

    @Test
    @DisplayName("cancel: happy path (มี reason แมป enum ได้) -> CANCELLED + set cancelledReason + updatedAt แล้ว save")
    void cancel_success_withReason() {
        Registration reg = newReg(88, 123, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        when(registrationRepository.findById(88)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.cancel(88, new RegistrationDto.CancelRequest("USER_CANCELLED"), 123);

        assertThat(saved.getRegistrationStatus()).isEqualTo(Registration.RegStatus.CANCELLED);
        assertThat(saved.getCancelledReason()).isEqualTo(Registration.CancelledReason.USER_CANCELLED);
        assertThat(saved.getUpdatedAt()).isNotNull();
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @DisplayName("cancel: null/invalid reason -> default USER_CANCELLED")
    void cancel_invalidReason_defaultsUserCancelled() {
        Registration reg = newReg(1, 123, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        var s1 = service.cancel(1, null, 123);
        assertThat(s1.getCancelledReason()).isEqualTo(Registration.CancelledReason.USER_CANCELLED);

        // invalid text
        reg.setRegistrationStatus(Registration.RegStatus.PENDING);
        var s2 = service.cancel(1, new RegistrationDto.CancelRequest("NOT_A_REASON"), 123);
        assertThat(s2.getCancelledReason()).isEqualTo(Registration.CancelledReason.USER_CANCELLED);
    }

    @Test
    @DisplayName("cancel: reg ไม่พบ -> IllegalArgumentException")
    void cancel_notFound() {
        when(registrationRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancel(1, null, 123))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registration not found");
    }

    @Test
    @DisplayName("cancel: ไม่ใช่เจ้าของ -> SecurityException('forbidden')")
    void cancel_forbidden() {
        Registration reg = newReg(1, 999, 1, 10, Registration.RegStatus.PENDING, Registration.PayStatus.UNPAID);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(reg));
        assertThatThrownBy(() -> service.cancel(1, null, 123))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("forbidden");
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancel: ถ้า CANCELLED อยู่แล้ว -> คืนค่าเดิมและไม่ save ซ้ำ")
    void cancel_alreadyCancelled_returnsAsIs() {
        Registration reg = newReg(1, 123, 1, 10, Registration.RegStatus.CANCELLED, Registration.PayStatus.UNPAID);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(reg));

        var returned = service.cancel(1, null, 123);

        assertThat(returned).isSameAs(reg);
        verify(registrationRepository, never()).save(any());
    }

    /* ===================== getByUserId ===================== */

    @Test
    @DisplayName("getByUserId: ไม่มี status -> เรียก findByUserIdAndPaymentStatusOrderByRegisteredAtDesc(userId, PAID) และ map เป็น DTO")
    void getByUserId_noStatus() {
        Integer userId = 123;

        Registration r1 = newReg(1, userId, 1, 10, Registration.RegStatus.CONFIRMED, Registration.PayStatus.PAID);
        Registration r2 = newReg(2, userId, 2, 20, Registration.RegStatus.CANCELLED, Registration.PayStatus.PAID);
        when(registrationRepository.findByUserIdAndPaymentStatusOrderByRegisteredAtDesc(eq(userId), eq(Registration.PayStatus.PAID)))
                .thenReturn(List.of(r1, r2));

        try (MockedStatic<RegistrationDto.Response> mocked = Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(r1)).thenReturn(respOf(r1));
            mocked.when(() -> RegistrationDto.Response.from(r2)).thenReturn(respOf(r2));

            var list = service.getByUserId(userId, null);

            assertThat(list).hasSize(2);
            assertThat(list.get(0).id()).isEqualTo(1);
            assertThat(list.get(1).id()).isEqualTo(2);
        }

        verify(registrationRepository).findByUserIdAndPaymentStatusOrderByRegisteredAtDesc(eq(userId), eq(Registration.PayStatus.PAID));
        verifyNoMoreInteractions(registrationRepository);
    }

    @Test
    @DisplayName("getByUserId: มี status ถูกต้อง -> เรียก findByUserIdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(userId, st, PAID)")
    void getByUserId_withValidStatus() {
        Integer userId = 7;
        String status = "CONFIRMED";

        Registration r = newReg(11, userId, 1, 2, Registration.RegStatus.CONFIRMED, Registration.PayStatus.PAID);
        when(registrationRepository.findByUserIdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(eq(userId),
                eq(Registration.RegStatus.CONFIRMED), eq(Registration.PayStatus.PAID)))
                .thenReturn(List.of(r));

        try (MockedStatic<RegistrationDto.Response> mocked = Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(r)).thenReturn(respOf(r));

            var list = service.getByUserId(userId, status);
            assertThat(list).hasSize(1);
            assertThat(list.get(0).registrationStatus()).isEqualTo("CONFIRMED");
        }

        verify(registrationRepository).findByUserIdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(eq(userId),
                eq(Registration.RegStatus.CONFIRMED), eq(Registration.PayStatus.PAID));
    }

    @Test
    @DisplayName("getByUserId: มี status แต่พิมพ์ผิด -> IllegalArgumentException('Invalid status: ...')")
    void getByUserId_invalidStatus() {
        assertThatThrownBy(() -> service.getByUserId(1, "XXX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
        verifyNoInteractions(registrationRepository);
    }

    /* ===================== getAllByEvent ===================== */

    @Test
    @DisplayName("getAllByEvent: ไม่มี status -> เรียก findByEvent_IdAndPaymentStatusOrderByRegisteredAtDesc(eventId, PAID)")
    void getAllByEvent_noStatus() {
        Integer eventId = 100;

        Registration r = newReg(5, 1, eventId, 10, Registration.RegStatus.CONFIRMED, Registration.PayStatus.PAID);
        when(registrationRepository.findByEvent_IdAndPaymentStatusOrderByRegisteredAtDesc(eq(eventId),
                eq(Registration.PayStatus.PAID)))
                .thenReturn(List.of(r));

        try (MockedStatic<RegistrationDto.Response> mocked = Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(r)).thenReturn(respOf(r));

            var list = service.getAllByEvent(eventId, null);
            assertThat(list).hasSize(1);
            assertThat(list.get(0).paymentStatus()).isEqualTo("PAID");
        }

        verify(registrationRepository).findByEvent_IdAndPaymentStatusOrderByRegisteredAtDesc(eq(eventId),
                eq(Registration.PayStatus.PAID));
    }

    @Test
    @DisplayName("getAllByEvent: มี status ถูกต้อง -> เรียก findByEvent_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(eventId, st, PAID)")
    void getAllByEvent_withValidStatus() {
        Integer eventId = 200;
        String status = "PENDING";

        Registration r = newReg(6, 2, eventId, 20, Registration.RegStatus.PENDING, Registration.PayStatus.PAID);
        when(registrationRepository.findByEvent_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
                eq(eventId), eq(Registration.RegStatus.PENDING), eq(Registration.PayStatus.PAID)
        )).thenReturn(List.of(r));

        try (MockedStatic<RegistrationDto.Response> mocked = Mockito.mockStatic(RegistrationDto.Response.class)) {
            mocked.when(() -> RegistrationDto.Response.from(r)).thenReturn(respOf(r));

            var list = service.getAllByEvent(eventId, status);
            assertThat(list).hasSize(1);
            assertThat(list.get(0).registrationStatus()).isEqualTo("PENDING");
        }

        verify(registrationRepository).findByEvent_IdAndRegistrationStatusAndPaymentStatusOrderByRegisteredAtDesc(
                eq(eventId), eq(Registration.RegStatus.PENDING), eq(Registration.PayStatus.PAID));
    }

    @Test
    @DisplayName("getAllByEvent: status พิมพ์ผิด -> IllegalArgumentException จาก Enum.valueOf")
    void getAllByEvent_invalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> service.getAllByEvent(1, "NOPE"));
        verifyNoInteractions(registrationRepository);
    }
}
