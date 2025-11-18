package com.example.eventproject.service;

import com.example.eventproject.dto.SessionDto;
import com.example.eventproject.dto.ZoneDto;
import com.example.eventproject.dto.ZoneTemplateDto;
import com.example.eventproject.model.EventSession;
import com.example.eventproject.repository.EventSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventSessionService {

    private final EventSessionRepository repo;
    private final ZoneTemplateService zoneTemplateService;

    public List<SessionDto> listByEvent(Integer eventId) {

        List<EventSession> sessions = repo.findWithZonesByEventId(eventId);
        var allTemplates = zoneTemplateService.getAllTemplates();   // โหลด template ทั้งหมด

        return sessions.stream()
                .map(s -> {

                    boolean isTemplate = s.isUseZoneTemplate();

                    // หา templateIds จากชื่อ zone
                    List<Integer> templateIds = isTemplate
                            ? s.getZones().stream()
                            .map(z -> allTemplates.stream()
                                    .filter(t -> t.name().equals(z.getName()))
                                    .map(ZoneTemplateDto::id)
                                    .findFirst()
                                    .orElse(null)
                            )
                            .filter(tid -> tid != null)
                            .distinct()
                            .toList()
                            : List.of();

                    // แปลง zone -> ZoneDto
                    List<ZoneDto> zones = s.getZones().stream()
                            .map(z -> new ZoneDto(
                                    z.getId(),
                                    z.getName(),
                                    z.getGroupName(),
                                    z.getCapacity(),
                                    z.getPrice()
                            ))
                            .toList();

                    return new SessionDto(
                            s.getId(),
                            s.getName(),
                            s.getStartTime(),
                            isTemplate,
                            templateIds,
                            zones
                    );
                })
                .toList();
    }
}



