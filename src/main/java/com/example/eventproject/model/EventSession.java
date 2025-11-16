package com.example.eventproject.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "event_sessions")
public class EventSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = true)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "use_zone_template", nullable = false)
    private boolean useZoneTemplate = false;

    // เชื่อมไป zones ทั้งหมดของ session นี้
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventZone> zones;

    // ====== Getters / Setters ======
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public boolean isUseZoneTemplate() { return useZoneTemplate; }
    public void setUseZoneTemplate(boolean useZoneTemplate) { this.useZoneTemplate = useZoneTemplate; }

    public List<EventZone> getZones() { return zones; }
    public void setZones(List<EventZone> zones) { this.zones = zones; }


}
