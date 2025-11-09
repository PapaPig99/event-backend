package com.example.eventproject.controller;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.service.EventZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final EventZoneService eventZoneService;

    /** GET /api/zones/session/{sessionId} */
    @GetMapping("/session/{sessionId}")
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

