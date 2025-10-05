package com.example.eventproject.controller;

import com.example.eventproject.dto.EventPublicDto;
import com.example.eventproject.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/events", produces = "application/json")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService service;

    @GetMapping("/{id}")
    public EventPublicDto getEvent(@PathVariable Integer id) {
        return service.getPublic(id);
    }
}
