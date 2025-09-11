package com.example.eventproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sessions", description = "รอบการแสดง/รอบงาน")
@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class SessionController {

    @Operation(summary = "รายการ session ของอีเวนต์")
    @GetMapping("/events/{eventId}/sessions")
    public ResponseEntity<?> listByEvent(@PathVariable Integer eventId) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "สร้าง session ใต้ event", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/events/{eventId}/sessions", consumes = "application/json")
    public ResponseEntity<?> create(@PathVariable Integer eventId, @RequestBody Object dto) {
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "แก้ไข session", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/sessions/{id}", consumes = "application/json")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Object dto) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "ลบ session", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return ResponseEntity.noContent().build();
    }
}
