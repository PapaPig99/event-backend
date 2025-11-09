package com.example.eventproject.controller;

import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.model.Registration;
import com.example.eventproject.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /* ==========================================================
       CREATE — สร้างการจองใหม่ (หลายใบใน zone เดียว)
       ========================================================== */
    @PostMapping
    public ResponseEntity<RegistrationDto.CreateResponse> create(
            @RequestParam String email,
            @RequestBody RegistrationDto.CreateRequest req
    ) {
        List<Registration> regs = registrationService.create(
                email,
                req.eventId(),
                req.sessionId(),
                req.zoneId(),
                req.quantity()
        );

        if (regs.isEmpty()) return ResponseEntity.badRequest().build();

        Registration sample = regs.get(0);
        RegistrationDto.CreateResponse response = new RegistrationDto.CreateResponse(
                sample.getPaymentReference(),
                sample.getEvent().getId(),
                sample.getSession().getId(),
                sample.getZone().getId(),
                sample.getZone().getName(),
                sample.getPrice(),
                regs.size(),
                sample.getTotalPrice(),
                sample.getPaymentStatus().name(),
                regs.stream().map(Registration::getTicketCode).collect(Collectors.toList())
        );

        return ResponseEntity.status(201).body(response);
    }

    /* ==========================================================
       CONFIRM PAYMENT — ยืนยันการชำระเงินทั้งชุด
       ========================================================== */
    @PatchMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody RegistrationDto.ConfirmRequest req) {
        try {
            List<Registration> updated = registrationService.confirmPayment(req.paymentReference());
            Registration first = updated.get(0);

            RegistrationDto.ConfirmResponse res = new RegistrationDto.ConfirmResponse(
                    first.getPaymentReference(),
                    first.getPaymentStatus().name(),
                    first.getPaidAt(),
                    first.getTotalPrice()
            );

            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* ==========================================================
       CHECK-IN (ADMIN) — ใช้ ticket code แทน id
       ========================================================== */
    @PatchMapping("/checkin/by-code")
    public ResponseEntity<?> checkInByTicketCode(@RequestParam String ticketCode) {
        try {
            Registration reg = registrationService.checkInByTicketCode(ticketCode);
            return ResponseEntity.ok("Ticket " + reg.getTicketCode() + " checked in successfully.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* ==========================================================
       READ — ดึงข้อมูลการจอง
       ========================================================== */

    /** ดึงการจองทั้งหมด (admin dashboard) */
    @GetMapping
    public ResponseEntity<List<Registration>> getAll() {
        return ResponseEntity.ok(registrationService.getAll());
    }

    /**
     *  ดึงการจองของผู้ใช้ (ทั้งหมด หรือกรองตามสถานะ)
     * ใช้ในหน้า "My Tickets" ของผู้ใช้
     *
     * ตัวอย่าง:
     *   /api/registrations/user/john@example.com      → ทั้งหมด
     *   /api/registrations/user/john@example.com?status=PAID  → เฉพาะจ่ายแล้ว
     *   /api/registrations/user/john@example.com?status=UNPAID → เฉพาะยังไม่จ่าย
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<List<Registration>> getUserRegistrations(
            @PathVariable String email,
            @RequestParam(required = false) String status
    ) {
        if (status == null) {
            // ทั้งหมด
            return ResponseEntity.ok(registrationService.getAllByUser(email));
        }

        try {
            Registration.PayStatus payStatus = Registration.PayStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(registrationService.getByUserAndStatus(email, payStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }


    /** ดึงการจองที่จ่ายแล้วของอีเวนต์ */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registration>> getPaidByEvent(@PathVariable Integer eventId) {
        return ResponseEntity.ok(registrationService.getPaidByEvent(eventId));
    }

    /** ดึงการจองที่จ่ายแล้วของอีเวนต์ + session */
    @GetMapping("/event/{eventId}/session/{sessionId}")
    public ResponseEntity<List<Registration>> getPaidByEventAndSession(
            @PathVariable Integer eventId,
            @PathVariable Integer sessionId) {
        return ResponseEntity.ok(registrationService.getPaidByEventAndSession(eventId, sessionId));
    }

    /* ==========================================================
       DELETE — ลบข้อมูลทั้งหมดของ event (admin)
       ========================================================== */
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<String> deleteAllByEvent(@PathVariable Integer eventId) {
        int deleted = registrationService.deleteAllByEventCascade(eventId);
        return ResponseEntity.ok("Deleted " + deleted + " registrations for event " + eventId);
    }
}
