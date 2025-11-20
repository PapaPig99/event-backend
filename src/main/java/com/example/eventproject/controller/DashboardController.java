package com.example.eventproject.controller;

import com.example.eventproject.dto.DashboardDto;
import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public DashboardDto getDashboard(@RequestParam(required = false) Integer eventId) {
        return service.getDashboard(eventId);
    }

    /** ตาราง Event list (capacity + sold) */
    @GetMapping("/events")
    public List<EventSalesSummary> getEventTable() {
        return service.getEventTable();
    }
}
