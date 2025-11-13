package com.example.eventproject.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.eventproject.model.Registration;
import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.EventRepository;
import com.example.eventproject.repository.EventSessionRepository;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.RegistrationRepository;
import com.example.eventproject.repository.RoleRepository;
import com.example.eventproject.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventSessionRepository sessionRepository;
    private final EventZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /* ==========================================================
     CREATE MULTI-TICKET REGISTRATION (Single Zone)
    - ผู้ใช้ที่สมัครแล้วจองเลย และผู้จองใหม่ให้สร้างในตารางไว้ก่อน (Guest)
   ========================================================== */
    @Transactional
    public List<Registration> create(String email,
                                     Integer eventId,
                                     Integer sessionId,
                                     Integer zoneId,
                                     Integer quantity) {

        // 1. ตรวจสอบจำนวน ticket ที่ต้องการจอง
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        // 2. normalize email
        String normalizedEmail = (email == null ? "" : email).trim().toLowerCase();
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // 3. ตรวจสอบว่ามีผู้ใช้นี้อยู่ในระบบหรือยัง — ถ้าไม่มีก็สร้าง guest
        User user = ensureUserExists(normalizedEmail);

        // 4. ตรวจสอบ entity หลักที่ต้องมี
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        var zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"));

        // 5. ตรวจสอบความจุโซน (รวมทั้ง Paid + Unpaid)
        int totalBooked = registrationRepository.countAllBookedInZone(zoneId);
        if (totalBooked + quantity > zone.getCapacity()) {
            throw new IllegalStateException("Zone " + zone.getName() + " is fully booked");
        }

        // 6. สร้างรหัสชำระเงินกลาง (ใช้ร่วมกันทุกใบ)
        String paymentRef = "PAY-" + LocalDate.now() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        List<Registration> tickets = new ArrayList<>();
        BigDecimal pricePerTicket = zone.getPrice();
        BigDecimal total = pricePerTicket.multiply(BigDecimal.valueOf(quantity));

        // 7. วนสร้าง Registration แยกใบตามจำนวนที่ผู้ใช้จอง
        for (int i = 1; i <= quantity; i++) {
            Registration reg = new Registration();
            reg.setEmail(normalizedEmail);       // ใช้ email ที่ normalize แล้ว
            reg.setUser(user);                   // ผูกกับ user ที่เป็น guest หรือ user จริง
            reg.setEvent(event);
            reg.setSession(session);
            reg.setZone(zone);
            reg.setPrice(pricePerTicket);
            reg.setTotalPrice(total);
            reg.setPaymentReference(paymentRef);
            reg.setPaymentStatus(Registration.PayStatus.UNPAID);
            reg.setTicketCode(generateTicketCode(event.getTitle(), i));
            reg.setCreatedAt(LocalDateTime.now());
            tickets.add(registrationRepository.save(reg));
        }

        return tickets;
    }

    /* ==========================================================
       AUTO-CREATE USER IF NOT EXISTS (Guest)
       ========================================================== */
    private User ensureUserExists(String normalizedEmail) {
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // ถ้ามี user แล้ว → ใช้ต่อได้เลย
        return userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    // ถ้าไม่มี → สร้าง guest user อัตโนมัติ
                    User guest = new User();
                    guest.setEmail(normalizedEmail);
                    guest.setName("Guest");
                    guest.setPassword(null); // ไม่มี password = guest ยังล็อกอินไม่ได้

                    // ดึง role GUEST (ถ้าไม่มีให้ fallback เป็น USER)
                    Role guestRole = roleRepository.findByCode("GUEST")
                            .or(() -> roleRepository.findByCode("USER"))
                            .orElseThrow(() -> new IllegalStateException("No default role found"));
                    guest.setRole(guestRole);

                    return userRepository.save(guest);
                });
    }

    /* ==========================================================
       CONFIRM PAYMENT — อัปเดตสถานะทั้งหมดในชุดเดียว
       ========================================================== */
    @Transactional
    public List<Registration> confirmPayment(String paymentReference) {
        var regs = registrationRepository.findByPaymentReference(paymentReference);
        if (regs.isEmpty())
            throw new IllegalArgumentException("Payment reference not found");

        LocalDateTime now = LocalDateTime.now();
        for (Registration reg : regs) {
            reg.setPaymentStatus(Registration.PayStatus.PAID);
            reg.setPaidAt(now);
        }
        return registrationRepository.saveAll(regs);
    }

    /* ==========================================================
       CHECK-IN — จาก ticket code
       ========================================================== */
    @Transactional
    public Registration checkInByTicketCode(String ticketCode) {
        var reg = registrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket code: " + ticketCode));

        if (Boolean.TRUE.equals(reg.getIsCheckedIn()))
            throw new IllegalStateException("Ticket already checked in");

        if (reg.getPaymentStatus() != Registration.PayStatus.PAID)
            throw new IllegalStateException("Ticket not paid yet");

        reg.setIsCheckedIn(true);
        reg.setCheckedInAt(LocalDateTime.now());
        return registrationRepository.save(reg);
    }

    /* ==========================================================
       READ / DELETE
       ========================================================== */
    @Transactional(readOnly = true)
    public List<Registration> getAll() {
        return registrationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Registration> getPaidByEvent(Integer eventId) {
        return registrationRepository.findByEvent_IdAndPaymentStatusOrderByCreatedAtDesc(
                eventId, Registration.PayStatus.PAID);
    }

    @Transactional(readOnly = true)
    public List<Registration> getAllByUser(String email) {
        return registrationRepository.findByEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Registration> getByUserAndStatus(String email, Registration.PayStatus status) {
        return registrationRepository.findByEmailAndPaymentStatusOrderByCreatedAtDesc(email, status);
    }

    @Transactional(readOnly = true)
    public List<Registration> getPaidByEventAndSession(Integer eventId, Integer sessionId) {
        return registrationRepository.findByEvent_IdAndSession_IdAndPaymentStatusOrderByCreatedAtDesc(
                eventId, sessionId, Registration.PayStatus.PAID);
    }

    @Transactional
    public int deleteAllByEventCascade(Integer eventId) {
        return registrationRepository.deleteAllByEventCascade(eventId);
    }

    /* ==========================================================
       UTILITIES  (ticket code)
       ========================================================== */
    private String generateTicketCode(String title, int seq) {
        String prefix = title.replaceAll("[^A-Za-z]", "").toUpperCase();
        String code;
        do {
            code = prefix.substring(0, Math.min(prefix.length(), 5))
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()
                    + "-" + String.format("%03d", seq);
        } while (registrationRepository.findByTicketCode(code).isPresent());
        return code;
    }

    /* ==========================================================
       READ BY PAYMENT REFERENCE
       ========================================================== */
    public List<Registration> getByPaymentReference(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new IllegalArgumentException("paymentReference is required");
        }
        return registrationRepository.findByPaymentReference(paymentReference);
    }
}
