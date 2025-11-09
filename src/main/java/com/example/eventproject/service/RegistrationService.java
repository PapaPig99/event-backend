package com.example.eventproject.service;

import com.example.eventproject.model.*;
import com.example.eventproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventSessionRepository sessionRepository;
    private final EventZoneRepository zoneRepository;
    private final UserRepository userRepository;

    /* ==========================================================
       CREATE MULTI-TICKET REGISTRATION (Single Zone)
       ========================================================== */
    @Transactional
    public List<Registration> create(String email,
                                     Integer eventId,
                                     Integer sessionId,
                                     Integer zoneId,
                                     Integer quantity) {

        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");

        // 1️ ตรวจสอบ entity
        var user = userRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        var zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"));

        // 2️ ตรวจสอบความจุโซน
        int totalBooked = registrationRepository.countAllBookedInZone(zoneId);
        if (totalBooked + quantity > zone.getCapacity()) {
            throw new IllegalStateException("Zone " + zone.getName() + " is fully booked");
        }

        // 3️ สร้างรหัสชำระเงินกลาง (ใช้ร่วมกันทุกใบ)
        String paymentRef = "PAY-" + LocalDateTime.now().toLocalDate() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        List<Registration> tickets = new ArrayList<>();
        BigDecimal pricePerTicket = zone.getPrice();
        BigDecimal total = pricePerTicket.multiply(BigDecimal.valueOf(quantity));

        // 4️ วนสร้าง ticket ตามจำนวน
        for (int i = 1; i <= quantity; i++) {
            Registration reg = new Registration();
            reg.setEmail(email);
            reg.setUser(user);
            reg.setEvent(event);
            reg.setSession(session);
            reg.setZone(zone);
            reg.setPrice(pricePerTicket);
            reg.setTotalPrice(total);
            reg.setPaymentReference(paymentRef);
            reg.setPaymentStatus(Registration.PayStatus.UNPAID);
            reg.setQuantity(1); // ใบละ 1
            reg.setTicketCode(generateTicketCode(event.getTitle(), i));
            reg.setCreatedAt(LocalDateTime.now());
            tickets.add(registrationRepository.save(reg));
        }

        return tickets;
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
}
