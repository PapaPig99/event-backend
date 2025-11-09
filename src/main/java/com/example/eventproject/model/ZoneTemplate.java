package com.example.eventproject.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity ของแม่แบบโซน (Zone Template)
 * ใช้สำหรับ clone ไปยัง EventZone ของแต่ละ session
 */
@Entity
@Table(name = "zone_templates")
public class ZoneTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "group_name")
    private String groupName;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // ===== Getters / Setters =====

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

}
