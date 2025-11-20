package com.example.eventproject.dto;

public record DashboardDto(
        long activeEvents,
        long ticketsSold,
        long totalRegistrations,
        long totalSignups,
        long dropOffs,
        long showRate,
        long checkIn
) {}


