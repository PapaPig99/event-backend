package com.example.eventproject.service;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.repository.EventZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventZoneService {

    private final EventZoneRepository zoneRepository;

    /* ==========================================================
       READ : ดูความจุ/จำนวนที่ถูกจองในแต่ละ session
       ========================================================== */
    @Transactional(readOnly = true)
    public List<ZoneAvailabilityDto> getAvailabilityBySession(Integer sessionId) {
        // ดึงข้อมูลโซนทั้งหมดใน session พร้อมยอดจอง/คงเหลือ
        return zoneRepository.findAvailabilityBySession(sessionId);
    }

    /* ==========================================================
       READ : ดูโซนทั้งหมดใน group เดียวกันของ session
       ========================================================== */
    @Transactional(readOnly = true)
    public List<EventZone> getZonesByGroup(Integer sessionId, String groupName) {
        return zoneRepository.findBySession_IdAndGroupName(sessionId, groupName);
    }

    /* ==========================================================
       READ : ดึงโซนทั้งหมดของ session (เช่น admin ใช้จัดการ)
       ========================================================== */
    @Transactional(readOnly = true)
    public List<EventZone> getZonesBySession(Integer sessionId) {
        return zoneRepository.findBySession_Id(sessionId);
    }
}
