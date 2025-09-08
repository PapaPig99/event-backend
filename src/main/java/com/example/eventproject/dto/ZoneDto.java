package com.example.eventproject.dto;

import java.math.BigDecimal;

public record ZoneDto(
        Integer id,
        String name,
        Integer capacity,
        BigDecimal price
) {}
