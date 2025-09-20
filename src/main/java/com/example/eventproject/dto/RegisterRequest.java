package com.example.eventproject.dto;

public record RegisterRequest(
        String email,
        String password,
        String name,
        String phone,
        String organization
) {}