package com.example.eventproject.dto;

import java.util.List;

public class OverviewResponse {
    private long activeEvents;
    private long ticketsSold;
    private List<EventSalesSummary> salesProgress;

    public OverviewResponse(long activeEvents, long ticketsSold, List<EventSalesSummary> salesProgress) {
        this.activeEvents = activeEvents; this.ticketsSold = ticketsSold; this.salesProgress = salesProgress;
    }
    public long getActiveEvents() { return activeEvents; }
    public long getTicketsSold() { return ticketsSold; }
    public List<EventSalesSummary> getSalesProgress() { return salesProgress; }
}
