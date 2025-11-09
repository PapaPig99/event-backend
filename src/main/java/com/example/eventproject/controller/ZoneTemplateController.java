package com.example.eventproject.controller;

import com.example.eventproject.model.ZoneTemplate;
import com.example.eventproject.service.ZoneTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller สำหรับจัดการ Zone Template (แม่แบบของโซน)
 * ใช้ในฝั่ง Admin ตอนสร้าง Session แบบใช้ Template
 */
@RestController
@RequestMapping("/api/zone-templates")
@RequiredArgsConstructor
public class ZoneTemplateController {

    private final ZoneTemplateService zoneTemplateService;

    /**
     * GET /api/zone-templates
     * ดึง Zone Template ทั้งหมด
     */
    @GetMapping
    public ResponseEntity<List<String>> getAllTemplateNames() {
        List<String> names = zoneTemplateService.getAllTemplateNames();
        return ResponseEntity.ok(names);
    }

    /**
     * POST /api/zone-templates/clone/{sessionId}
     * clone template zone ทั้งหมดลงใน session ที่กำหนด
     */
    @PostMapping("/clone/{sessionId}")
    public ResponseEntity<String> cloneZonesToSession(@PathVariable Integer sessionId) {
        zoneTemplateService.cloneZonesToSession(sessionId);
        return ResponseEntity.ok("Zones cloned successfully to session " + sessionId);
    }
}
