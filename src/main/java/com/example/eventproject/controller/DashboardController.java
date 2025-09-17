package com.example.eventproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dashboard", description = "สรุป/รายงานยอดขาย (Admin)")
@RestController
@RequestMapping(value = "/api", produces = "application/json")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Operation(summary = "ตัวเลขรวมของวันนี้: activeEvents, ticketsSold, totalRevenue")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "ยอดขายต่ออีเวนต์ (capacity, sold, revenue, pending)")
    @GetMapping("/dashboard/sales-progress")
    public ResponseEntity<?> salesProgress() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Analytics รายโซนในอีเวนต์ (capacity, sold, pending)")
    @GetMapping("/events/{eventId}/analytics")
    public ResponseEntity<?> analytics(@PathVariable Integer eventId,
                                       @RequestParam(required = false) Integer sessionId) {
        return ResponseEntity.ok().build();
    }
}
