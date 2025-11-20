package com.example.eventproject.service;

import com.example.eventproject.dto.DashboardDto;
import com.example.eventproject.dto.EventSalesSummary;
import com.example.eventproject.model.Event;
import com.example.eventproject.repository.DashboardRepository;
import com.example.eventproject.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final DashboardRepository repo;
    private final EventRepository eventRepo;


    public DashboardService(DashboardRepository repo, EventRepository eventRepo) {
        this.repo = repo;
        this.eventRepo = eventRepo;
    }

    public DashboardDto getDashboard(Integer eventId) {

        // --------------------------
        // All Events Summary
        // --------------------------
        if (eventId == null) {

            long activeEvents = repo.countActiveEvents();
            long ticketsSold = repo.countTicketsSoldAll();
            long totalRegistrations = repo.countRegistrationsAll();
            long checkIn = repo.countCheckinAll();

            long totalSignups = totalRegistrations;       // all regis
            long dropOffs = totalRegistrations - ticketsSold;
            long showRate = ticketsSold == 0 ? 0 : (checkIn * 100 / ticketsSold);

            return new DashboardDto(
                    activeEvents,
                    ticketsSold,
                    totalRegistrations,
                    totalSignups,
                    dropOffs,
                    showRate,
                    checkIn
            );
        }

        // --------------------------
        // Specific Event Summary
        // --------------------------
        long totalSignups = repo.countSignupsByEvent(eventId);
        long dropOffs = repo.countDropoffsByEvent(eventId);
        long ticketsSold = repo.countTicketsSoldByEvent(eventId);
        long checkIn = repo.countCheckinByEvent(eventId);

        long showRate = ticketsSold == 0 ? 0 : (checkIn * 100 / ticketsSold);

        return new DashboardDto(
                0,               // activeEvents (ไม่เกี่ยวเมื่อ filter event)
                ticketsSold,
                totalSignups,    // totalRegistrations = signups ของ event นี้
                totalSignups,
                dropOffs,
                showRate,
                checkIn
        );
    }
    public List<EventSalesSummary> getEventTable() {
        List<Event> events = eventRepo.findAll();

        return events.stream().map(ev -> {
            long capacity = repo.sumCapacityByEvent(ev.getId());
            long sold = repo.countSoldByEvent(ev.getId());

            return new EventSalesSummary(
                    ev.getId(),
                    ev.getTitle(),
                    ev.getCategory(),
                    capacity,
                    sold
            );
        }).toList();
    }

}
