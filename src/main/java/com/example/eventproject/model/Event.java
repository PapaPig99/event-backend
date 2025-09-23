package com.example.eventproject.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String category;
    private String location;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    private LocalDateTime saleStartAt;
    private LocalDateTime saleEndAt;
    private boolean saleUntilSoldout;

    @Column(name = "door_open_time")
    private String doorOpenTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String posterImageUrl;
    private String detailImageUrl;
    private String seatmapImageUrl;
    @Column(name = "created_by_user_id", nullable = false)
    private Integer createdByUserId;
    // Relations
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime asc")
    private Set<EventSession> sessions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name asc")
    private Set<EventZone> zones = new LinkedHashSet<>();

    // --- getter/setter ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getSaleStartAt() { return saleStartAt; }
    public void setSaleStartAt(LocalDateTime saleStartAt) { this.saleStartAt = saleStartAt; }

    public LocalDateTime getSaleEndAt() { return saleEndAt; }
    public void setSaleEndAt(LocalDateTime saleEndAt) { this.saleEndAt = saleEndAt; }

    public boolean isSaleUntilSoldout() { return saleUntilSoldout; }
    public void setSaleUntilSoldout(boolean saleUntilSoldout) { this.saleUntilSoldout = saleUntilSoldout; }

    public String getDoorOpenTime() { return doorOpenTime; }
    public void setDoorOpenTime(String doorOpenTime) { this.doorOpenTime = doorOpenTime; }

    public String getPosterImageUrl() { return posterImageUrl; }
    public void setPosterImageUrl(String posterImageUrl) { this.posterImageUrl = posterImageUrl; }

    public String getDetailImageUrl() { return detailImageUrl; }
    public void setDetailImageUrl(String detailImageUrl) { this.detailImageUrl = detailImageUrl; }

    public String getSeatmapImageUrl() { return seatmapImageUrl; }
    public void setSeatmapImageUrl(String seatmapImageUrl) { this.seatmapImageUrl = seatmapImageUrl; }

    public Set<EventSession> getSessions() { return sessions; }
    public void setSessions(Set<EventSession> sessions) { this.sessions = sessions; }

    public Set<EventZone> getZones() { return zones; }
    public void setZones(Set<EventZone> zones) { this.zones = zones; }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }
    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
