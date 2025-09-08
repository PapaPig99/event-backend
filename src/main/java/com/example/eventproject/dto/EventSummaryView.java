package com.example.eventproject.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.eventproject.model.Status;
import java.time.LocalTime;

public interface EventSummaryView {
    Integer getId();
    String getTitle();
    String getCategory();
    String getLocation();
    LocalDate getStartDate();
    LocalDate getEndDate();
    Status getStatus();
    LocalDateTime getSaleStartAt();
    LocalDateTime getSaleEndAt();
    Boolean getSaleUntilSoldout();
    LocalTime getDoorOpenTime();
    String getPosterImageUrl();
    String getDetailImageUrl();
    String getSeatmapImageUrl();
}

