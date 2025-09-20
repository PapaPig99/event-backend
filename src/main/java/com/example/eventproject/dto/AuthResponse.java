package com.example.eventproject.dto;

public record AuthResponse(String token, Long userId, String email, String name, String role) {}

