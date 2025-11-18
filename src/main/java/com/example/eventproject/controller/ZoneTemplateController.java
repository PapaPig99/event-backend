package com.example.eventproject.controller;

import com.example.eventproject.dto.ZoneTemplateDto;
import com.example.eventproject.service.ZoneTemplateService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zone-templates")
@RequiredArgsConstructor
public class ZoneTemplateController {

    private final ZoneTemplateService zoneTemplateService;

    /* ==========================================================
       GET — Templates ทั้งหมด
       ========================================================== */
    // ดึง Zone Templates ทั้งหมด
    @GetMapping
    public ResponseEntity<List<ZoneTemplateDto>> getAll() {
        return ResponseEntity.ok(zoneTemplateService.getAllTemplates());
    }

    /* ==========================================================
       POST — Create Template
       ========================================================== */
    // สร้าง Zone Template ใหม่
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ZoneTemplateDto> create(@RequestBody ZoneTemplateDto dto) {
        ZoneTemplateDto created = zoneTemplateService.createTemplate(dto);
        return ResponseEntity.status(201).body(created);
    }

    /* ==========================================================
       PUT — Update Template
       ========================================================== */
    // แก้ไข Zone Template
    @PutMapping("/{id}")
    public ResponseEntity<ZoneTemplateDto> update(
            @PathVariable Integer id,
            @RequestBody ZoneTemplateDto dto
    ) {
        return ResponseEntity.ok(zoneTemplateService.updateTemplate(id, dto));
    }

    /* ==========================================================
       DELETE — Delete Template
       ========================================================== */
    // ลบ Zone Template
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        zoneTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /* ==========================================================
       POST — Clone Templates → Session
       ========================================================== */

    @PostMapping("/clone-to-session/{sessionId}")
    public ResponseEntity<?> cloneToSession(
            @PathVariable Integer sessionId,
            @RequestBody List<Integer> templateIds
    ) {
        zoneTemplateService.cloneSpecificTemplatesToSession(sessionId, templateIds);
        return ResponseEntity.ok().body("Templates cloned into session " + sessionId);
    }

}
