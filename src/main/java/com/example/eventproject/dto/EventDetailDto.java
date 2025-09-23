package com.example.eventproject.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.example.eventproject.model.Status;

public record EventDetailDto(
        Integer id,
        String title,
        String category,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        Status status,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        Boolean saleUntilSoldout,
        String doorOpenTime,
        String description,
        String posterImageUrl,
        String detailImageUrl,
        String seatmapImageUrl,
        List<SessionDto> sessions,
        List<ZoneDto> zones
) {}


