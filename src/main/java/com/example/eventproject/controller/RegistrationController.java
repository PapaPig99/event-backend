package com.example.eventproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Registrations", description = "การจอง/ซื้อตั๋ว")
@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class RegistrationController {

    @Operation(summary = "Hold ที่นั่ง (สร้างรายการจอง)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/registrations", consumes = "application/json")
    public ResponseEntity<?> create(@RequestBody Object dto,
                                    @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "ดูรายการจองของผู้ใช้ที่ล็อกอิน", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/registrations/me")
    public ResponseEntity<?> myRegistrations(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "ยกเลิกรายการจอง", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/registrations/{id}/cancel", consumes = "application/json")
    public ResponseEntity<?> cancel(@PathVariable Integer id, @RequestBody(required = false) Object body) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "ยืนยันหลังจ่ายเงินสำเร็จ", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/registrations/{id}/confirm", consumes = "application/json")
    public ResponseEntity<?> confirm(@PathVariable Integer id, @RequestBody Object body) {
        return ResponseEntity.ok().build();
    }
}
