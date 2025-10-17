package com.example.eventproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.service.DashboardService;

import lombok.RequiredArgsConstructor;
import java.util.List;

//สรุป/รายงานEvents (Admin)
@RestController
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    //ตัวเลขรวมของวันนี้: activeEvents, ticketsSold
    @GetMapping("/dashboard/summary")
    public ResponseEntity<OverviewResponse> summary() {
        return ResponseEntity.ok(service.getOverview());
    }

    //ต่ออีเวนต์ (capacity, sold)
    @GetMapping("/dashboard/sales-progress")
    public ResponseEntity<List<EventSalesSummary>> salesProgress() {
        return ResponseEntity.ok(service.getOverview().getSalesProgress());

    }
    @GetMapping("/events/{eventId}/analytics")
    public ResponseEntity<Void> analytics(
            @PathVariable Integer eventId,
            @RequestParam(value = "sessionId", required = false) Integer sessionId
    ) {
        // intentionally empty: return 200 with empty body
        return ResponseEntity.ok().build();
    }
}
