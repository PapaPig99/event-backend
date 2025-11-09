package com.example.eventproject.dto;

import java.math.BigDecimal;

public record ZoneAvailabilityDto(
        Integer zoneId,
        String zoneName,
        Integer capacity,
        Long booked,
        Long available,
        BigDecimal price
) {}
