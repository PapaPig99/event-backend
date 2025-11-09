package com.example.eventproject.service;

import com.example.eventproject.dto.ZoneAvailabilityDto;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.repository.EventZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service สำหรับจัดการข้อมูลโซน (EventZone)
 * เช่น ดูความจุ, จำนวนที่ถูกจอง, หมายเลขที่นั่งที่ถูกจอง, และกลุ่มโซน
 */
@Service
@RequiredArgsConstructor
public class EventZoneService {

    private final EventZoneRepository zoneRepository;

    /* ==========================================================
       READ : ดูความจุ/การจองในแต่ละ session
       ========================================================== */
    @Transactional(readOnly = true)
    public List<ZoneAvailabilityDto> getAvailabilityBySession(Integer sessionId) {

        // ดึงข้อมูลโซนทั้งหมดพร้อมจำนวนที่ถูกจอง และราคา
        var availabilityList = zoneRepository.findAvailabilityBySession(sessionId);

        // ดึงหมายเลขที่นั่งที่ถูกจองแล้วใน session เดียวกัน
        var bookedNums = zoneRepository.findBookedSeatNumbers(sessionId);

        // สร้าง DTO สำหรับส่งกลับ โดยแยกกรณีมี/ไม่มีที่นั่ง
        return availabilityList.stream().map(zone -> {
            if (zone.hasSeatNumbers()) {
                // ถ้ามีหมายเลขที่นั่ง → ใส่รายการเลขที่จองแล้ว
                return new ZoneAvailabilityDto(
                        zone.zoneId(),
                        zone.zoneName(),
                        zone.capacity(),
                        zone.booked(),
                        zone.available(),
                        zone.hasSeatNumbers(),
                        bookedNums,
                        zone.price()
                );
            } else {
                // ถ้าไม่มีหมายเลขที่นั่ง → bookedSeatNumbers เป็น List เปล่า
                return new ZoneAvailabilityDto(
                        zone.zoneId(),
                        zone.zoneName(),
                        zone.capacity(),
                        zone.booked(),
                        zone.available(),
                        zone.hasSeatNumbers(),
                        List.of(),
                        zone.price()
                );
            }
        }).toList();
    }

    /* ==========================================================
       READ : ดูโซนทั้งหมดใน group เดียวกัน
       ========================================================== */
    @Transactional(readOnly = true)
    public List<EventZone> getZonesByGroup(Integer sessionId, String groupName) {
        return zoneRepository.findBySession_IdAndGroupName(sessionId, groupName);
    }
}
