package com.example.eventproject.service;

import com.example.eventproject.model.Event;
import com.example.eventproject.model.EventSession;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.model.Registration;
import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.EventRepository;
import com.example.eventproject.repository.EventSessionRepository;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.RegistrationRepository;
import com.example.eventproject.repository.RoleRepository;
import com.example.eventproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    RegistrationRepository registrationRepository;
    @Mock
    EventRepository eventRepository;
    @Mock
    EventSessionRepository sessionRepository;
    @Mock
    EventZoneRepository zoneRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    RegistrationService service;

    /* ===================== helpers ===================== */

    private Event newEvent(Integer id, String title) {
        Event e = new Event();
        // ถ้า Event มี setId(Integer) อยู่จริง (ซึ่งปกติ JPA entity จะมี)
        try {
            Event.class.getMethod("setId", Integer.class).invoke(e, id);
        } catch (Exception ignored) {
            // ถ้าไม่มี setId ก็ไม่เป็นไร สำหรับ logic ที่ test ใช้ แค่ให้ getId() คืนค่า id ได้ในโค้ดจริง
        }
        e.setTitle(title);
        return e;
    }

    private EventSession newSession(Integer id) {
        EventSession s = new EventSession();
        try {
            EventSession.class.getMethod("setId", Integer.class).invoke(s, id);
        } catch (Exception ignored) {
        }
        return s;
    }

    private EventZone newZone(Integer id, String name, BigDecimal price, int capacity) {
        EventZone z = new EventZone();
        try {
            EventZone.class.getMethod("setId", Integer.class).invoke(z, id);
        } catch (Exception ignored) {
        }
        z.setName(name);
        z.setPrice(price);
        z.setCapacity(capacity);
        return z;
    }

    // User helper ไม่แตะ id เลย ป้องกันปัญหาไม่มี field/setter
    private User newUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    /* ===================== create() ===================== */

    @Test
    @DisplayName("create: happy path — normalize email, ใช้ user เดิม, เช็ก capacity และสร้าง ticket ตามจำนวน")
    void create_success_existingUser() {
        String emailInput = "  USER@Test.com  ";
        String normalized = "user@test.com";
        Integer eventId = 1;
        Integer sessionId = 10;
        Integer zoneId = 5;
        Integer quantity = 2;

        // user เดิมในระบบ
        Role role = new Role();
        try {
            Role.class.getMethod("setId", Integer.class).invoke(role, 1);
        } catch (Exception ignored) {
        }
        role.setCode("USER");
        User existingUser = newUser(normalized, role);
        when(userRepository.findByEmail(normalized)).thenReturn(Optional.of(existingUser));

        // event / session / zone
        Event event = newEvent(eventId, "DevConf");
        EventSession session = newSession(sessionId);
        EventZone zone = newZone(zoneId, "VIP", new BigDecimal("100.00"), 10);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));

        // มีจองไปแล้ว 3 ที่นั่ง
        when(registrationRepository.countAllBookedInZone(zoneId)).thenReturn(3);

        // save() ให้ set id (ถ้ามี) แล้วคืน object เดิมกลับมา
        Answer<Registration> saveAnswer = inv -> {
            Registration r = inv.getArgument(0);
            try {
                var getIdMethod = Registration.class.getMethod("getId");
                Object currentId = getIdMethod.invoke(r);
                if (currentId == null) {
                    var setIdMethod = Registration.class.getMethod("setId", Integer.class);
                    setIdMethod.invoke(r, 1000);
                }
            } catch (Exception ignored) {
            }
            return r;
        };
        when(registrationRepository.save(any(Registration.class))).thenAnswer(saveAnswer);

        List<Registration> tickets = service.create(emailInput, eventId, sessionId, zoneId, quantity);

        // assert
        assertThat(tickets).hasSize(2);

        // ทุกใบควรผูก user / event / session / zone เดียวกัน
        for (Registration r : tickets) {
            assertThat(r.getEmail()).isEqualTo(normalized);
            assertThat(r.getUser()).isSameAs(existingUser);
            assertThat(r.getEvent()).isSameAs(event);
            assertThat(r.getSession()).isSameAs(session);
            assertThat(r.getZone()).isSameAs(zone);
            assertThat(r.getPaymentStatus()).isEqualTo(Registration.PayStatus.UNPAID);
            assertThat(r.getTicketCode()).isNotBlank();
            assertThat(r.getCreatedAt()).isNotNull();
            assertThat(r.getPrice()).isEqualByComparingTo("100.00");
        }

        // totalPrice ของแต่ละใบ = price * quantity ทั้งชุด
        assertThat(tickets.get(0).getTotalPrice()).isEqualByComparingTo("200.00");

        // paymentReference ต้องไม่ว่าง และทุกใบใช้ ref เดียวกัน
        String ref = tickets.get(0).getPaymentReference();
        assertThat(ref).isNotBlank();
        assertThat(tickets.get(1).getPaymentReference()).isEqualTo(ref);

        verify(userRepository).findByEmail(normalized);
        verify(eventRepository).findById(eventId);
        verify(sessionRepository).findById(sessionId);
        verify(zoneRepository).findById(zoneId);
        verify(registrationRepository).countAllBookedInZone(zoneId);
        verify(registrationRepository, times(2)).save(any(Registration.class));
        // ❌ อย่าใช้ verifyNoMoreInteractions(registrationRepository) เพราะ generateTicketCode() เรียก findByTicketCode()
    }

    @Test
    @DisplayName("create: quantity <= 0 -> IllegalArgumentException('Quantity must be > 0')")
    void create_invalidQuantity() {
        assertThatThrownBy(() -> service.create("test@test.com", 1, 1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be > 0");

        verifyNoInteractions(userRepository, eventRepository, sessionRepository,
                zoneRepository, registrationRepository, roleRepository);
    }

    @Test
    @DisplayName("create: email ว่าง/เป็น null -> IllegalArgumentException('Email is required')")
    void create_invalidEmail() {
        assertThatThrownBy(() -> service.create("   ", 1, 1, 1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");

        assertThatThrownBy(() -> service.create(null, 1, 1, 1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");

        verifyNoInteractions(userRepository, eventRepository, sessionRepository,
                zoneRepository, registrationRepository, roleRepository);
    }

    @Test
    @DisplayName("create: capacity ไม่พอ -> IllegalStateException('Zone ... is fully booked')")
    void create_zoneCapacityExceeded() {
        String email = "a@test.com";
        Integer eventId = 1, sessionId = 2, zoneId = 3, quantity = 5;

        // user
        Role role = new Role();
        try {
            Role.class.getMethod("setId", Integer.class).invoke(role, 1);
        } catch (Exception ignored) {
        }
        role.setCode("USER");
        User u = newUser(email, role);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));

        Event event = newEvent(eventId, "TestEvent");
        EventSession session = newSession(sessionId);
        EventZone zone = newZone(zoneId, "A", new BigDecimal("100.00"), 6); // capacity 6

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));

        // จองไปแล้ว 3 ที่ -> 3 + 5 = 8 > 6
        when(registrationRepository.countAllBookedInZone(zoneId)).thenReturn(3);

        assertThatThrownBy(() -> service.create(email, eventId, sessionId, zoneId, quantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Zone " + zone.getName() + " is fully booked");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: ไม่มี user เดิม -> สร้าง guest user ใหม่โดยใช้ role GUEST")
    void create_createGuestUserWhenNotExist() {
        String email = "guest@test.com";
        String normalized = email;

        when(userRepository.findByEmail(normalized)).thenReturn(Optional.empty());

        Role guestRole = new Role();
        try {
            Role.class.getMethod("setId", Integer.class).invoke(guestRole, 99);
        } catch (Exception ignored) {
        }
        guestRole.setCode("GUEST");

        when(roleRepository.findByCode("GUEST")).thenReturn(Optional.of(guestRole));

        // save guest user: ไม่ยุ่งกับ id เลย ปล่อยให้เป็น null ก็ได้
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // event, session, zone
        Event event = newEvent(1, "Event");
        EventSession session = newSession(2);
        EventZone zone = newZone(3, "Z", new BigDecimal("10.00"), 10);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(sessionRepository.findById(2)).thenReturn(Optional.of(session));
        when(zoneRepository.findById(3)).thenReturn(Optional.of(zone));
        when(registrationRepository.countAllBookedInZone(3)).thenReturn(0);

        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Registration> list = service.create(email, 1, 2, 3, 1);

        assertThat(list).hasSize(1);
        Registration r = list.get(0);
        assertThat(r.getUser()).isNotNull();
        assertThat(r.getUser().getRole()).isSameAs(guestRole);
        assertThat(r.getEmail()).isEqualTo(normalized);

        verify(userRepository).findByEmail(normalized);
        verify(userRepository).save(any(User.class));
        verify(roleRepository).findByCode("GUEST");
    }

    /* ===================== confirmPayment() ===================== */

    @Test
    @DisplayName("confirmPayment: happy path — ตั้งสถานะ PAID และ set paidAt ให้ทุก registration ในชุดเดียวกัน")
    void confirmPayment_success() {
        String ref = "PAY-" + LocalDate.now() + "-ABC123";

        Registration r1 = new Registration();
        Registration r2 = new Registration();
        r1.setPaymentStatus(Registration.PayStatus.UNPAID);
        r2.setPaymentStatus(Registration.PayStatus.UNPAID);

        when(registrationRepository.findByPaymentReference(ref)).thenReturn(List.of(r1, r2));
        when(registrationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Registration> list = service.confirmPayment(ref);

        assertThat(list).hasSize(2);
        assertThat(list)
                .allSatisfy(r -> {
                    assertThat(r.getPaymentStatus()).isEqualTo(Registration.PayStatus.PAID);
                    assertThat(r.getPaidAt()).isNotNull();
                });

        verify(registrationRepository).findByPaymentReference(ref);
        verify(registrationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("confirmPayment: ถ้าไม่เจอ paymentReference -> IllegalArgumentException")
    void confirmPayment_notFound() {
        when(registrationRepository.findByPaymentReference("X")).thenReturn(List.of());

        assertThatThrownBy(() -> service.confirmPayment("X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment reference not found");
    }

    /* ===================== checkInByEventSessionAndCode() ===================== */

    @Test
    @DisplayName("checkInByEventSessionAndCode: happy path — event/session ถูกต้อง, จ่ายเงินแล้ว, ยังไม่เคยเช็กอิน")
    void checkIn_success() {
        Integer eventId = 1;
        Integer sessionId = 10;
        String code = "TICKET-001";

        Event event = newEvent(eventId, "Test");
        EventSession session = newSession(sessionId);

        Registration reg = new Registration();
        reg.setEvent(event);
        reg.setSession(session);
        reg.setPaymentStatus(Registration.PayStatus.PAID);
        reg.setIsCheckedIn(false);
        reg.setTicketCode(code);

        when(registrationRepository.findByTicketCode(code)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration saved = service.checkInByEventSessionAndCode(eventId, sessionId, code);

        assertThat(saved.getIsCheckedIn()).isTrue();
        assertThat(saved.getCheckedInAt()).isNotNull();

        verify(registrationRepository).findByTicketCode(code);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @DisplayName("checkInByEventSessionAndCode: event ไม่ตรง -> IllegalArgumentException")
    void checkIn_wrongEvent() {
        String code = "CODE";
        Registration reg = new Registration();
        reg.setEvent(newEvent(999, "Other"));
        reg.setSession(newSession(10));
        reg.setPaymentStatus(Registration.PayStatus.PAID);
        when(registrationRepository.findByTicketCode(code)).thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> service.checkInByEventSessionAndCode(1, 10, code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ticket does not belong to this event");
    }

    @Test
    @DisplayName("checkInByEventSessionAndCode: ยังไม่จ่ายเงิน -> IllegalStateException")
    void checkIn_unpaid() {
        String code = "CODE";
        Registration reg = new Registration();
        reg.setEvent(newEvent(1, "E"));
        reg.setSession(newSession(10));
        reg.setPaymentStatus(Registration.PayStatus.UNPAID);

        when(registrationRepository.findByTicketCode(code)).thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> service.checkInByEventSessionAndCode(1, 10, code))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has not been paid");
    }

    @Test
    @DisplayName("checkInByEventSessionAndCode: เช็กอินแล้ว -> IllegalStateException")
    void checkIn_alreadyCheckedIn() {
        String code = "CODE";
        Registration reg = new Registration();
        reg.setEvent(newEvent(1, "E"));
        reg.setSession(newSession(10));
        reg.setPaymentStatus(Registration.PayStatus.PAID);
        reg.setIsCheckedIn(true);

        when(registrationRepository.findByTicketCode(code)).thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> service.checkInByEventSessionAndCode(1, 10, code))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already checked-in");
    }

    /* ===================== getByPaymentReference() ===================== */

    @Test
    @DisplayName("getByPaymentReference: happy path — เรียก repository ด้วยค่าเดิม แล้วคืน list เดิม")
    void getByPaymentReference_success() {
        String ref = "PAY-XXX";
        Registration r1 = new Registration();
        Registration r2 = new Registration();
        when(registrationRepository.findByPaymentReference(ref)).thenReturn(List.of(r1, r2));

        List<Registration> list = service.getByPaymentReference(ref);

        assertThat(list).hasSize(2);
        verify(registrationRepository).findByPaymentReference(ref);
    }

    @Test
    @DisplayName("getByPaymentReference: paymentReference ว่าง/null -> IllegalArgumentException")
    void getByPaymentReference_invalidInput() {
        assertThatThrownBy(() -> service.getByPaymentReference(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentReference is required");

        assertThatThrownBy(() -> service.getByPaymentReference("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentReference is required");

        verifyNoInteractions(registrationRepository);
    }
}
