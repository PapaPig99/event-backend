package com.example.eventproject.dto;

import com.example.eventproject.model.Status;
import java.math.BigDecimal;
import java.time.LocalTime;

public record SessionDto(
        Integer id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        Status status,
        Integer maxParticipants,
        BigDecimal price
) {}
