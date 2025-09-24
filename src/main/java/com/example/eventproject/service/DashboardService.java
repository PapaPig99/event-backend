package com.example.eventproject.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.repository.DashboardRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository repo;

    public OverviewResponse getOverview() {
        long activeEvents = repo.countActiveEvents();
        long ticketsSold  = repo.sumTicketsSold();

        List<EventSalesSummary> rows = repo.findSalesRows().stream()
            .map(r -> new EventSalesSummary(
                r.getEventId(), r.getTitle(), r.getCategory(),
                r.getCapacity() == null ? 0L : r.getCapacity(),
                r.getSold() == null ? 0L : r.getSold()
            ))
            .collect(Collectors.toList());

        return new OverviewResponse(activeEvents, ticketsSold, rows);
    }
}
