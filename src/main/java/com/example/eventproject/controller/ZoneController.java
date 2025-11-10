package com.example.eventproject.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.service.EventZoneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final EventZoneService eventZoneService;

    /** GET /api/zones/session/{sessionId}/availability */
    @GetMapping("/session/{sessionId}/availability")
    public ResponseEntity<List<ZoneAvailabilityDto>> getAvailabilityBySession(
            @PathVariable Integer sessionId) {
        var result = eventZoneService.getAvailabilityBySession(sessionId);
        return ResponseEntity.ok(result);
    }

    /**  GET /api/zones/group?sessionId=1&groupName=VIP */
    @GetMapping("/group")
    public ResponseEntity<List<EventZone>> getZonesByGroup(
            @RequestParam Integer sessionId,
            @RequestParam String groupName) {
        var result = eventZoneService.getZonesByGroup(sessionId, groupName);
        return ResponseEntity.ok(result);
    }
}

