package com.example.eventproject.dto;

import com.example.eventproject.model.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record EventUpsertRequest(
        String title,
        String category,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        Status status,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        Boolean saleUntilSoldout,
        LocalTime doorOpenTime,
        String posterImageUrl,
        String detailImageUrl,
        String seatmapImageUrl,
        Integer createdByUserId,
        List<SessionDto> sessions,
        List<ZoneDto> zones
) {}
