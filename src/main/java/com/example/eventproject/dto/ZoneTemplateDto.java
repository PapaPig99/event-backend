package com.example.eventproject.dto;
import java.math.BigDecimal;

public record ZoneTemplateDto(
        Integer id,
        String name,
        String groupName,
        Integer capacity,
        BigDecimal price
) { }
