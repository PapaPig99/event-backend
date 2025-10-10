package com.example.eventproject.dto;

import java.time.LocalTime;

import com.example.eventproject.model.Status;

public record SessionDto(
        Integer id,
        String name,
        LocalTime startTime,
        Status status
) {}
