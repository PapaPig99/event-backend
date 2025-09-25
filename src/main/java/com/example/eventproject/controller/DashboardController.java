package com.example.eventproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.service.DashboardService;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "Dashboard", description = "สรุป/รายงานEvents (Admin)")
@RestController
@RequestMapping(value = "/api", produces = "application/json")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @Operation(summary = "ตัวเลขรวมของวันนี้: activeEvents, ticketsSold")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<OverviewResponse> summary() {
        return ResponseEntity.ok(service.getOverview());
    }

    @Operation(summary = "ยอดขายต่ออีเวนต์ (capacity, sold)")
    @GetMapping("/dashboard/sales-progress")
    public ResponseEntity<List<EventSalesSummary>> salesProgress() {
        return ResponseEntity.ok(service.getOverview().getSalesProgress());
    }

    @Operation(summary = "Analytics รายโซนในอีเวนต์ (capacity, sold, pending)")
    @GetMapping("/events/{eventId}/analytics")
    public ResponseEntity<?> analytics(@PathVariable Integer eventId,
                                       @RequestParam(required = false) Integer sessionId) {
        // TODO: ทำ service.getEventAnalytics(eventId, sessionId) ในลำดับถัดไป
        return ResponseEntity.ok().build();
    }
}
