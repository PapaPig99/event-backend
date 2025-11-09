package com.example.eventproject.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.eventproject.model.Status;

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
    String getDoorOpenTime();
    String getDescription();
    String getPosterImageUrl();
    String getSeatmapImageUrl();
    LocalDateTime getCreatedAt();
}
