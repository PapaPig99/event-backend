package com.example.eventproject.dto;

import java.math.BigDecimal;
import java.util.List;

public record ZoneAvailabilityDto(
        Integer zoneId,
        String zoneName,
        Integer capacity,
        Long booked,
        Long available,
        boolean hasSeatNumbers,
        List<Integer> bookedSeatNumbers,
        BigDecimal price
) {}
