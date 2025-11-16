package com.example.eventproject.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.model.Registration;
import com.example.eventproject.service.RegistrationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /* ==========================================================
       CREATE — รองรับ (single zone) และ (items[])
       - รองรับ guest ที่ยังไม่สมัครสมาชิก
       ========================================================== */
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam String email,
            @RequestBody Map<String, Object> body
    ) {
        try {
            Integer eventId = asInt(body.get("eventId"));
            Integer sessionId = asInt(body.get("sessionId"));

            if (eventId == null || sessionId == null) {
                return ResponseEntity.badRequest().body("eventId and sessionId are required");
            }

            List<Registration> regs = new ArrayList<>();

            // items[] (หลาย zone หลาย quantity)
            Object itemsObj = body.get("items");
            if (itemsObj instanceof List<?> items && !items.isEmpty()) {
                for (Object o : items) {
                    if (!(o instanceof Map<?, ?> it)) continue;

                    Integer zoneId = asInt(it.get("seatZoneId"));
                    if (zoneId == null) zoneId = asInt(it.get("zoneId"));
                    Integer qty = asInt(it.get("quantity"));

                    if (zoneId == null)
                        return ResponseEntity.badRequest().body("seatZoneId/zoneId is required");
                    if (qty == null || qty <= 0)
                        return ResponseEntity.badRequest().body("Quantity must be > 0");

                    // รองรับ guest auto-create ผ่าน service
                    regs.addAll(registrationService.create(email, eventId, sessionId, zoneId, qty));
                }
            } else {
                // โหมดเดิม: zoneId + quantity เดียว
                Integer zoneId = asInt(body.get("seatZoneId"));
                if (zoneId == null) zoneId = asInt(body.get("zoneId"));
                Integer qty = asInt(body.get("quantity"));

                if (zoneId == null)
                    return ResponseEntity.badRequest().body("seatZoneId/zoneId is required");
                if (qty == null || qty <= 0)
                    return ResponseEntity.badRequest().body("Quantity must be > 0");

                regs = registrationService.create(email, eventId, sessionId, zoneId, qty);
            }

            if (regs.isEmpty()) return ResponseEntity.badRequest().body("No registration created");

            // สร้าง response ตามโครงสร้างใหม่ (ใช้ใบแรกเป็น sample)
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

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }

    /* ==========================================================
       CONFIRM PAYMENT — ยืนยันการชำระเงินทั้งชุด
       ========================================================== */
    @PatchMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody RegistrationDto.ConfirmRequest req) {
        try {
            List<Registration> updated = registrationService.confirmPayment(req.paymentReference());
            if (updated == null || updated.isEmpty()) {
                return ResponseEntity.badRequest().body("paymentReference not found");
            }

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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    /* ==========================================================================
   CHECK-IN — เช็คอินตาม Event + Session + TicketCode
   ========================================================================== */
    @PatchMapping("/events/{eventId}/sessions/{sessionId}/checkin/{ticketCode}")
    public ResponseEntity<?> checkInByTicketCode(
            @PathVariable Integer eventId,
            @PathVariable Integer sessionId,
            @PathVariable String ticketCode
    ) {
        try {
            Registration updated = registrationService.checkInByEventSessionAndCode(eventId, sessionId, ticketCode);
            return ResponseEntity.ok(RegistrationDto.Response.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage())); // already checked-in
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


    /* ==========================================================
       CANCEL (stub/demo)
       ========================================================== */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        Map<String, Object> ok = new HashMap<>();
        ok.put("id", id);
        ok.put("status", "CANCELLED");
        return ResponseEntity.ok(ok);
    }

    /* ==========================================================
       ME — ดึงรายการจองของผู้ใช้ (ใช้ email)
       - แสดงทุกใบ รวม guest ที่จองก่อนสมัคร
       ========================================================== */
    @GetMapping("/me")
    public ResponseEntity<?> myRegs(
            @RequestParam(required = false) String email,
            @AuthenticationPrincipal(expression = "email") String jwtEmail,
            @RequestParam(required = false) String status
    ) {
        try {
            String finalEmail = null;
            if (email != null && !email.isBlank()) {
                finalEmail = email.trim().toLowerCase();
            } else if (jwtEmail != null && !jwtEmail.isBlank()) {
                finalEmail = jwtEmail.trim().toLowerCase();
            }

            if (finalEmail == null) {
                return ResponseEntity.badRequest().body("Email missing");
            }

            List<RegistrationDto.Response> list;

            if (status == null || status.isBlank()) {
                list = registrationService.getAllByUser(finalEmail);
            } else {
                Registration.PayStatus ps = Registration.PayStatus.valueOf(status.toUpperCase());
                list = registrationService.getByUserAndStatus(finalEmail, ps);
            }

            return ResponseEntity.ok(list);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }



    /* ==========================================================
       GET ONE — ใช้เปิด Ticket Modal (ดูใบเดียว)
       ========================================================== */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Integer id) {
        try {
            Registration hit = registrationService.getAll().stream()
                    .filter(r -> Objects.equals(r.getId(), id))
                    .findFirst()
                    .orElse(null);
            if (hit == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(RegistrationDto.Response.from(hit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /* ==========================================================
       READ
       ========================================================== */
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Registration> list = registrationService.getAll();
        return ResponseEntity.ok(
                list.stream().map(RegistrationDto.Response::from).toList()
        );
    }


    @GetMapping("/user/{email}")
    public ResponseEntity<List<RegistrationDto.Response>> getUserRegistrations(
            @PathVariable String email,
            @RequestParam(required = false) String status
    ) {
        if (status == null) {
            return ResponseEntity.ok(registrationService.getAllByUser(email));
        }
        try {
            Registration.PayStatus payStatus = Registration.PayStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(registrationService.getByUserAndStatus(email, payStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }


    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RegistrationDto.Response>> getPaidByEvent(@PathVariable Integer eventId) {
        return ResponseEntity.ok(registrationService.getPaidByEvent(eventId));
    }

    @GetMapping("/event/{eventId}/session/{sessionId}")
    public ResponseEntity<List<RegistrationDto.Response>> getPaidByEventAndSession(
            @PathVariable Integer eventId,
            @PathVariable Integer sessionId
    ) {
        return ResponseEntity.ok(registrationService.getPaidByEventAndSession(eventId, sessionId));
    }

    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<String> deleteAllByEvent(@PathVariable Integer eventId) {
        int deleted = registrationService.deleteAllByEventCascade(eventId);
        return ResponseEntity.ok("Deleted " + deleted + " registrations for event " + eventId);
    }

    
    /* ==========================================================
   VIEW TICKETS BY PAYMENT REFERENCE
   - ใช้สำหรับแสดงตั๋วทุกใบใน 1 รอบการจ่าย
   - read-only ไม่แก้สถานะอะไรทั้งนั้น
   ========================================================== */
@GetMapping("/by-ref/{paymentReference}")
public ResponseEntity<?> getByPaymentReference(@PathVariable String paymentReference) {
    try {
        List<Registration> regs = registrationService.getByPaymentReference(paymentReference);

        if (regs == null || regs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // ใช้ DTO เดิมที่เอาไว้แสดงรายละเอียดตั๋ว
        List<RegistrationDto.Response> out = regs.stream()
                .map(RegistrationDto.Response::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(out);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}


    /* --------------------- helpers --------------------- */
    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try { return Integer.parseInt(s.trim()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }



    
}
