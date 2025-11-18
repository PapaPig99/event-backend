package com.example.eventproject.dto;

import java.time.LocalTime;
import java.util.List;
public record SessionUpsertDto(
        Integer id,
        String name,
        LocalTime startTime,
        Boolean useZoneTemplate,
        List<Integer> templateIds,
        List<ZoneDto> zones
) {}
