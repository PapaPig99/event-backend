package com.example.eventproject.dto;

    public record EventSalesSummary(
            Integer eventId,
            String title,
            String category,
            long capacity,
            long sold
    ) {}
