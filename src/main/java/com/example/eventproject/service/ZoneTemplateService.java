package com.example.eventproject.service;

import com.example.eventproject.dto.ZoneTemplateDto;
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
 * Service สำหรับจัดการ Zone Template (CRUD)
 */
@Service
@RequiredArgsConstructor
public class ZoneTemplateService {

    private final ZoneTemplateRepository templateRepository;
    private final EventZoneRepository zoneRepository;
    private final EventSessionRepository sessionRepository;

    /* ==========================================================
       READ : template ทั้งหมด
       ========================================================== */
    @Transactional(readOnly = true)
    public List<ZoneTemplateDto> getAllTemplates() {
        return templateRepository.findAll()
                .stream()
                .map(t -> new ZoneTemplateDto(
                        t.getId(),
                        t.getName(),
                        t.getGroupName(),
                        t.getCapacity(),
                        t.getPrice()
                ))
                .toList();
    }

    /* ==========================================================
       CREATE : เพิ่ม template ใหม่
       ========================================================== */
    @Transactional
    public ZoneTemplateDto createTemplate(ZoneTemplateDto dto) {
        ZoneTemplate tpl = new ZoneTemplate();
        tpl.setName(dto.name());
        tpl.setGroupName(dto.groupName());
        tpl.setCapacity(dto.capacity());
        tpl.setPrice(dto.price());

        ZoneTemplate saved = templateRepository.save(tpl);

        return new ZoneTemplateDto(
                saved.getId(),
                saved.getName(),
                saved.getGroupName(),
                saved.getCapacity(),
                saved.getPrice()
        );
    }

    /* ==========================================================
       UPDATE : แก้ไข template
       ========================================================== */
    @Transactional
    public ZoneTemplateDto updateTemplate(Integer id, ZoneTemplateDto dto) {
        ZoneTemplate tpl = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));

        tpl.setName(dto.name());
        tpl.setGroupName(dto.groupName());
        tpl.setCapacity(dto.capacity());
        tpl.setPrice(dto.price());

        ZoneTemplate saved = templateRepository.save(tpl);

        return new ZoneTemplateDto(
                saved.getId(),
                saved.getName(),
                saved.getGroupName(),
                saved.getCapacity(),
                saved.getPrice()
        );
    }

    /* ==========================================================
       DELETE : ลบ template
       ========================================================== */
    @Transactional
    public void deleteTemplate(Integer id) {
        if (!templateRepository.existsById(id)) {
            throw new IllegalArgumentException("Template not found: " + id);
        }
        templateRepository.deleteById(id);
    }

    /* ==========================================================
       CLONE : คัดลอกจาก template
       ========================================================== */
    @Transactional
    public void cloneSpecificTemplatesToSession(Integer sessionId, List<Integer> templateIds) {

        if (templateIds == null || templateIds.isEmpty()) {
            throw new IllegalArgumentException("templateIds is empty for session " + sessionId);
        }

        EventSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // ดึง template เฉพาะที่เลือก
        List<ZoneTemplate> templates = templateRepository.findAllById(templateIds);

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
