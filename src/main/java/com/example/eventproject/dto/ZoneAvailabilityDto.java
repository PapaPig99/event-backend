package com.example.eventproject.dto;

public record ZoneAvailabilityDto(
        Integer zoneId,
        String zoneName,
        Integer capacity,
        Long booked,
        Long available
) {}
