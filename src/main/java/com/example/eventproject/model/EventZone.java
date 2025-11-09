package com.example.eventproject.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "event_zones")
public class EventZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private EventSession session;

    @Column(nullable = false)
    private String name;

    private String groupName;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private BigDecimal price;


    // ===== Getters / Setters =====

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public EventSession getSession() { return session; }
    public void setSession(EventSession session) { this.session = session; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
