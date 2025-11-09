package com.example.eventproject.dto;

import com.example.eventproject.model.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        String seatmapImageUrl,
        LocalDateTime createdAt,
        List<SessionDto> sessions
) {}
