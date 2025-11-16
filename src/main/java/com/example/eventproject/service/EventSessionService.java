package com.example.eventproject.service;

import com.example.eventproject.dto.SessionDto;
import com.example.eventproject.dto.ZoneDto;
import com.example.eventproject.model.EventSession;
import com.example.eventproject.repository.EventSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class EventSessionService {

    private final EventSessionRepository repo;

    public List<SessionDto> listByEvent(Integer eventId) {

        // ดึง session พร้อม zones
        List<EventSession> sessions = repo.findWithZonesByEventId(eventId);

        return sessions.stream()
                .map(s -> new SessionDto(
                        s.getId(),
                        s.getName(),
                        s.getStartTime(),
                        s.isUseZoneTemplate(),
                        s.getZones().stream()
                                .map(z -> new ZoneDto(
                                        z.getId(),
                                        z.getName(),
                                        z.getGroupName(),
                                        z.getCapacity(),
                                        z.getPrice()
                                ))
                                .toList()
                ))
                .toList();

    }
}


