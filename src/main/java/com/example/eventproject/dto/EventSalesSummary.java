package com.example.eventproject.dto;

public class EventSalesSummary {
    private Integer id;
    private String title;
    private String category;
    private Long capacity;
    private Long sold;

    public EventSalesSummary(Integer id, String title, String category, Long capacity, Long sold) {
        this.id = id; this.title = title; this.category = category;
        this.capacity = capacity; this.sold = sold;
    }
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public Long getCapacity() { return capacity; }
    public Long getSold() { return sold; }
}
