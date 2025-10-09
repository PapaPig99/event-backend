package com.example.eventproject.controller;

import com.example.eventproject.service.EventZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final EventZoneService zoneService;

    @GetMapping("/session/{sessionId}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(zoneService.getAvailabilityBySession(sessionId));
    }
}
