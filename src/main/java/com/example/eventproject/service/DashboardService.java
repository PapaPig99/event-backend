package com.example.eventproject.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.dto.OverviewResponse;
import com.example.eventproject.dto.DashboardFullResponse;
import com.example.eventproject.repository.DashboardRepository;
import com.example.eventproject.repository.DashboardRepository.EventSalesRow;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository repo;

    // ====== ของเพื่อน (เดิม) ======
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

    // ====== ของใหม่ สำหรับ /dashboard/full-summary ======
    public DashboardFullResponse getFullOverview() {
        OverviewResponse base = getOverview();

        long totalRegistration = repo.sumTotalRegistrations();
        long totalSignups      = totalRegistration;
        long dropOffs          = Math.max(0, totalRegistration - base.getTicketsSold());
        long showRate          = (totalRegistration == 0)
                ? 0
                : Math.round(100.0 * base.getTicketsSold() / totalRegistration);
        long checkIn           = 0; // ถ้ามี table checkin จริงค่อยต่อเพิ่มได้

        return new DashboardFullResponse(
                base.getActiveEvents(),
                base.getTicketsSold(),
                totalRegistration,
                totalSignups,
                dropOffs,
                showRate,
                checkIn,
                base.getSalesProgress()
        );
    }
}
