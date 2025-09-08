package com.example.eventproject.service;

import com.example.eventproject.dto.*;
import com.example.eventproject.model.Event;
import com.example.eventproject.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepo;

    // GET ALL
    @Transactional(readOnly = true)
    public List<EventSummaryView> list() {
        return eventRepo.findAllByOrderByStartDateAsc();
    }

    // GET id
    @Transactional(readOnly = true)
    public EventDetailDto get(Integer id) {
        Event e = eventRepo.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        var sessions = e.getSessions().stream()
                .map(s -> new SessionDto(
                        s.getId(), s.getName(), s.getStartTime(), s.getEndTime(),
                        s.getStatus(), s.getMaxParticipants(), s.getPrice()
                )).toList();

        var zones = e.getZones().stream()
                .map(z -> new ZoneDto(z.getId(), z.getName(), z.getCapacity(), z.getPrice()))
                .toList();

        return new EventDetailDto(
                e.getId(), e.getTitle(), e.getCategory(), e.getLocation(),
                e.getStartDate(), e.getEndDate(), e.getStatus(),
                e.getSaleStartAt(), e.getSaleEndAt(), e.isSaleUntilSoldout(), // <-- ตรงนี้ใช้ is...
                e.getDoorOpenTime(), e.getPosterImageUrl(), e.getDetailImageUrl(),
                e.getSeatmapImageUrl(), sessions, zones
        );
    }
    // CREATE
    @Transactional
    public Integer create(EventUpsertDto dto) {
        Event e = new Event();
        apply(e, dto);
        return eventRepo.save(e).getId();
    }

    // UPDATE
    @Transactional
    public void update(Integer id, EventUpsertDto dto) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        apply(e, dto);
        eventRepo.save(e);
    }

    // DELETE
    @Transactional
    public void delete(Integer id) {
        if (!eventRepo.existsById(id)) {
            throw new IllegalArgumentException("Event not found: " + id);
        }
        eventRepo.deleteById(id);
    }

    // mapper: DTO → Entity
    private static void apply(Event e, EventUpsertDto dto) {
        e.setTitle(dto.title());
        e.setCategory(dto.category());
        e.setLocation(dto.location());
        e.setStartDate(dto.startDate());
        e.setEndDate(dto.endDate());
        e.setStatus(dto.status());
        e.setSaleStartAt(dto.saleStartAt());
        e.setSaleEndAt(dto.saleEndAt());
        e.setSaleUntilSoldout(dto.saleUntilSoldout());
        e.setDoorOpenTime(dto.doorOpenTime());
        e.setPosterImageUrl(dto.posterImageUrl());
        e.setDetailImageUrl(dto.detailImageUrl());
        e.setSeatmapImageUrl(dto.seatmapImageUrl());
        e.setCreatedByUserId(dto.createdByUserId());
    }
}

