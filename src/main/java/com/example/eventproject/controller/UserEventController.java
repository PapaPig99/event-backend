package com.example.eventproject.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventproject.model.UserEvent;
import com.example.eventproject.repo.UserEventRepository;

@RestController
public class UserEventController {

    private final UserEventRepository repo;

    public UserEventController(UserEventRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/user-events")
    public List<UserEvent> all() {
        return repo.findAll();
    }
}
