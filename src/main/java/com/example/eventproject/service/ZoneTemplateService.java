package com.example.eventproject.service;

import com.example.eventproject.model.EventSession;
import com.example.eventproject.model.EventZone;
import com.example.eventproject.model.ZoneTemplate;
import com.example.eventproject.repository.EventSessionRepository;
import com.example.eventproject.repository.EventZoneRepository;
import com.example.eventproject.repository.ZoneTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service สำหรับจัดการ Zone Template
 * ใช้ตอน admin ต้องการ clone zone template ไปยัง session ใหม่
 */
@Service
@RequiredArgsConstructor
public class ZoneTemplateService {

    private final ZoneTemplateRepository templateRepository;
    private final EventZoneRepository zoneRepository;
    private final EventSessionRepository sessionRepository;

    /* ==========================================================
       READ : ดึงชื่อ template ทั้งหมด (เช่น สำหรับ dropdown)
       ========================================================== */
    @Transactional(readOnly = true)
    public List<String> getAllTemplateNames() {
        return templateRepository.findAll()
                .stream()
                .map(ZoneTemplate::getName)
                .toList();
    }

    /* ==========================================================
       CREATE : Clone zone template ทั้งหมดลงใน session ที่เลือก
       ========================================================== */
    @Transactional
    public void cloneZonesToSession(Integer sessionId) {
        // 1. หา session เป้าหมาย
        EventSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // 2. ดึง zone templates ทั้งหมดจาก master table
        var templates = templateRepository.findAll();

        // 3. สร้าง EventZone ใหม่ตาม template ทีละอัน
        for (ZoneTemplate tpl : templates) {
            EventZone zone = new EventZone();
            zone.setSession(session);
            zone.setName(tpl.getName());
            zone.setGroupName(tpl.getGroupName());
            zone.setCapacity(tpl.getCapacity());
            zone.setPrice(tpl.getPrice());
            zoneRepository.save(zone);
        }
    }
}
