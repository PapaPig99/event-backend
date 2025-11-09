package com.example.eventproject.controller;

import com.example.eventproject.model.Registration;
import com.example.eventproject.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller สำหรับจัดการการจองตั๋ว (Registration)
 * รองรับ: จอง, ยืนยันชำระเงิน, เช็กอิน, ดูข้อมูล
 */
@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /* ==========================================================
       CREATE : สร้างการจองใหม่
       ========================================================== */
    @PostMapping
    public ResponseEntity<Registration> create(@RequestParam String email,
                                               @RequestParam Integer eventId,
                                               @RequestParam Integer sessionId,
                                               @RequestParam Integer zoneId,
                                               @RequestParam(required = false) Integer seatNumber,
                                               @RequestParam(defaultValue = "1") Integer quantity) {

        Registration reg = registrationService.create(email, eventId, sessionId, zoneId, seatNumber, quantity);
        return ResponseEntity.status(201).body(reg);
    }

    /* ==========================================================
       CONFIRM PAYMENT : ยืนยันการชำระเงิน
       ========================================================== */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Registration> confirmPayment(@PathVariable Integer id) {
        Registration reg = registrationService.confirmPayment(id);
        return ResponseEntity.ok(reg);
    }

    /* ==========================================================
    CHECK-IN (ADMIN INPUT) — ใช้ ticket code แทน id
    ========================================================== */
    @PatchMapping("/checkin/by-code")
    public ResponseEntity<?> checkInByTicketCode(@RequestParam String ticketCode) {
        try {
            Registration reg = registrationService.checkInByTicketCode(ticketCode);
            return ResponseEntity.ok("Ticket " + ticketCode + " checked in successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid ticket code: " + ticketCode);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("bad Request" + e.getMessage());
        }
    }



    /* ==========================================================
       READ : ดึงข้อมูลการจอง
       ========================================================== */
    // ดึงการจองทั้งหมด (admin)
    @GetMapping
    public ResponseEntity<List<Registration>> getAll() {
        return ResponseEntity.ok(registrationService.getAll());
    }

    // ดึงการจองของผู้ใช้ที่จ่ายแล้ว
    @GetMapping("/user/{email}")
    public ResponseEntity<List<Registration>> getPaidByUser(@PathVariable String email) {
        return ResponseEntity.ok(registrationService.getPaidByUser(email));
    }

    // ดึงการจองที่จ่ายแล้วของอีเวนต์
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registration>> getPaidByEvent(@PathVariable Integer eventId) {
        return ResponseEntity.ok(registrationService.getPaidByEvent(eventId));
    }

    // ดึงการจองที่จ่ายแล้วของอีเวนต์ + session
    @GetMapping("/event/{eventId}/session/{sessionId}")
    public ResponseEntity<List<Registration>> getPaidByEventAndSession(
            @PathVariable Integer eventId,
            @PathVariable Integer sessionId) {
        return ResponseEntity.ok(registrationService.getPaidByEventAndSession(eventId, sessionId));
    }

    /* ==========================================================
       DELETE : ลบการจองทั้งหมดของ event (admin)
       ========================================================== */
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<String> deleteAllByEvent(@PathVariable Integer eventId) {
        int deleted = registrationService.deleteAllByEventCascade(eventId);
        return ResponseEntity.ok("Deleted " + deleted + " registrations for event " + eventId);
    }
}
