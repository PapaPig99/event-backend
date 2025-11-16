package com.example.eventproject.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.eventproject.service.EventSessionService;


@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class SessionController {

    private final EventSessionService service;

    public SessionController(EventSessionService service) {
        this.service = service;
    }

    // รายการ session ของอีเวนต์
    @GetMapping("/events/{eventId}/sessions")
    public ResponseEntity<?> listByEvent(@PathVariable Integer eventId) {

        var list = service.listByEvent(eventId);

        return ResponseEntity.ok(list);
    }
}

