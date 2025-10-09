package com.example.eventproject.service;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.repository.EventZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventZoneService {

    private final EventZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public List<ZoneAvailabilityDto> getAvailabilityBySession(Integer sessionId) {
        return zoneRepository.findAvailabilityBySession(sessionId);
    }
}
