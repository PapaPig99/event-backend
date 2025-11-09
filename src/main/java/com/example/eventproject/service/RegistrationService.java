package com.example.eventproject.service;

import com.example.eventproject.model.*;
import com.example.eventproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service ชั้นกลางสำหรับจัดการการจองตั๋ว (Registration)
 * รวม logic ธุรกิจ เช่น ตรวจความจุ, ตรวจที่นั่งซ้ำ, อัปเดตสถานะการชำระเงิน
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventSessionRepository sessionRepository;
    private final EventZoneRepository zoneRepository;
    private final UserRepository userRepository;

    /* ==========================================================
       CREATE REGISTRATION — สร้างรายการจองใหม่
       ========================================================== */
    @Transactional
    public Registration create(String email,
                               Integer eventId,
                               Integer sessionId,
                               Integer zoneId,
                               Integer seatNumber,
                               Integer quantity) {

        // 1️ ตรวจสอบว่าผู้ใช้, event, session, zone มีอยู่จริง
        var user = userRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        var zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"));

        // 2 ตรวจสอบจำนวนที่จองแล้วในโซนนี้ (รวม UNPAID + PAID)
        int totalBooked = registrationRepository.countAllBookedInZone(zoneId);
        if (totalBooked + quantity > zone.getCapacity()) {
            throw new IllegalStateException("Zone " + zone.getName() + " is fully booked");
        }

        // 3️ ถ้าโซนนี้เป็นแบบมีหมายเลขที่นั่ง ต้องตรวจที่ซ้ำด้วย
        if (zone.getHasSeatNumbers() != null && zone.getHasSeatNumbers() && seatNumber != null) {
            int existing = registrationRepository.countExistingSeat(zoneId, seatNumber);
            if (existing > 0) {
                throw new IllegalStateException("Seat number " + seatNumber + " is already booked");
            }
        }


        // 4️สร้าง registration entity ใหม่
        Registration reg = new Registration();
        reg.setEmail(email);
        reg.setUser(user);
        reg.setEvent(event);
        reg.setSession(session);
        reg.setZone(zone);
        reg.setQuantity(quantity != null ? quantity : 1);
        reg.setSeatNumber(seatNumber);
        reg.setTotalPrice(zone.getPrice().multiply(
                java.math.BigDecimal.valueOf(reg.getQuantity())
        ));
        reg.setTicketCode(generateTicketCode());
        reg.setPaymentStatus(Registration.PayStatus.UNPAID);
        reg.setCreatedAt(LocalDateTime.now());

        // 5️ บันทึกลงฐานข้อมูล
        return registrationRepository.save(reg);
    }

    /* ==========================================================
       CONFIRM PAYMENT — ยืนยันการชำระเงิน
       ========================================================== */
    @Transactional
    public Registration confirmPayment(Integer registrationId) {
        // หา registration ที่ต้องการอัปเดต
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // อัปเดตสถานะเป็น “PAID” และบันทึกเวลาชำระ
        reg.setPaymentStatus(Registration.PayStatus.PAID);
        reg.setPaidAt(LocalDateTime.now());
        return registrationRepository.save(reg);
    }

    /* ==========================================================
    CHECK-IN BY TICKET CODE — สำหรับ Admin กรอก ticket code
    ========================================================== */
    @Transactional
    public Registration checkInByTicketCode(String ticketCode) {
        // หา registration จาก ticket code
        Registration reg = registrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket code: " + ticketCode));

        // ตรวจสอบว่ายังไม่ได้เช็กอิน
        if (Boolean.TRUE.equals(reg.getIsCheckedIn())) {
            throw new IllegalStateException("Ticket " + ticketCode + " has already been checked in.");
        }

        // ตรวจสอบสถานะการชำระเงิน
        if (reg.getPaymentStatus() != Registration.PayStatus.PAID) {
            throw new IllegalStateException("Ticket " + ticketCode + " has not been paid yet.");
        }

        // บันทึกเวลา check-in
        reg.setIsCheckedIn(true);
        reg.setCheckedInAt(LocalDateTime.now());

        return registrationRepository.save(reg);
    }



    /* ==========================================================
       READ — ดึงข้อมูลการจองในรูปแบบต่าง ๆ
       ========================================================== */
    @Transactional(readOnly = true)
    public List<Registration> getAll() {
        // ดึงการจองทั้งหมด (ใช้ในหน้า admin)
        return registrationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Registration> getPaidByEvent(Integer eventId) {
        // ดึงเฉพาะรายการที่จ่ายแล้วของอีเวนต์
        return registrationRepository.findByEvent_IdAndPaymentStatusOrderByCreatedAtDesc(
                eventId, Registration.PayStatus.PAID);
    }

    @Transactional(readOnly = true)
    public List<Registration> getPaidByUser(String email) {
        // ดึงเฉพาะรายการที่จ่ายแล้วของผู้ใช้
        return registrationRepository.findByEmailAndPaymentStatusOrderByCreatedAtDesc(
                email, Registration.PayStatus.PAID);
    }

    @Transactional(readOnly = true)
    public List<Registration> getPaidByEventAndSession(Integer eventId, Integer sessionId) {
        // ดึงเฉพาะรายการที่จ่ายแล้วของอีเวนต์ + รอบ (session)
        return registrationRepository.findByEvent_IdAndSession_IdAndPaymentStatusOrderByCreatedAtDesc(
                eventId, sessionId, Registration.PayStatus.PAID);
    }

    /* ==========================================================
       DELETE — ลบข้อมูลทั้งหมดของอีเวนต์ (เมื่อ event ถูกลบ)
       ========================================================== */
    @Transactional
    public int deleteAllByEventCascade(Integer eventId) {
        return registrationRepository.deleteAllByEventCascade(eventId);
    }

    /* ==========================================================
       UTILITIES — ฟังก์ชันช่วย เช่น generate ticket code
       ========================================================== */
    private String generateTicketCode() {
        String code;
        do {
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (registrationRepository.findByTicketCode(code).isPresent());
        return code;
    }
}
