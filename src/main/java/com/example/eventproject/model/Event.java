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

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CLOSED;

    @Column(name = "sale_start_at")
    private LocalDateTime saleStartAt;

    @Column(name = "sale_end_at")
    private LocalDateTime saleEndAt;

    @Column(name = "sale_until_soldout")
    private boolean saleUntilSoldout;

    @Column(name = "door_open_time")
    private String doorOpenTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "seatmap_image_url")
    private String seatmapImageUrl;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // --- Relations ---
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime asc")
    private Set<EventSession> sessions = new LinkedHashSet<>();

    // --- Constructors ---
    public Event() {}
    public Event(Integer id) { this.id = id; }

    // --- Getters/Setters ---
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPosterImageUrl() { return posterImageUrl; }
    public void setPosterImageUrl(String posterImageUrl) { this.posterImageUrl = posterImageUrl; }

    public String getSeatmapImageUrl() { return seatmapImageUrl; }
    public void setSeatmapImageUrl(String seatmapImageUrl) { this.seatmapImageUrl = seatmapImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<EventSession> getSessions() { return sessions; }
    public void setSessions(Set<EventSession> sessions) { this.sessions = sessions; }
}
