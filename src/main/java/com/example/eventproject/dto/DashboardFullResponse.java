// src/main/java/com/example/eventproject/dto/DashboardFullResponse.java
package com.example.eventproject.dto;

import java.util.List;

public class DashboardFullResponse {

    private long activeEvents;
    private long ticketsSold;
    private long totalRegistration;
    private long totalSignups;
    private long dropOffs;
    private long showRate;
    private long checkIn;

    private List<EventSalesSummary> salesProgress;

    public DashboardFullResponse(
            long activeEvents,
            long ticketsSold,
            long totalRegistration,
            long totalSignups,
            long dropOffs,
            long showRate,
            long checkIn,
            List<EventSalesSummary> salesProgress
    ) {
        this.activeEvents      = activeEvents;
        this.ticketsSold       = ticketsSold;
        this.totalRegistration = totalRegistration;
        this.totalSignups      = totalSignups;
        this.dropOffs          = dropOffs;
        this.showRate          = showRate;
        this.checkIn           = checkIn;
        this.salesProgress     = salesProgress;
    }

    public long getActiveEvents() { return activeEvents; }
    public long getTicketsSold() { return ticketsSold; }
    public long getTotalRegistration() { return totalRegistration; }
    public long getTotalSignups() { return totalSignups; }
    public long getDropOffs() { return dropOffs; }
    public long getShowRate() { return showRate; }
    public long getCheckIn() { return checkIn; }
    public List<EventSalesSummary> getSalesProgress() { return salesProgress; }
}
