// src/main/java/com/example/eventproject/controller/AdminEventController.java
package com.example.eventproject.controller;

import com.example.eventproject.dto.EventDetailAdmin;
import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.dto.EventUpsertRequest;
import com.example.eventproject.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/events", produces = "application/json")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // ถ้ามี JWT
public class AdminEventController {

    private final EventService service;

    // LIST
    @GetMapping
    public List<EventSummaryView> list() {
        return service.list(); // ของเดิมคุณใช้ได้เลย
    }

    // DETAIL (ADMIN)
    @GetMapping("/{id}")
    public EventDetailAdmin get(@PathVariable Integer id) {
        return service.getAdmin(id); // <-- เมธอดฝั่ง admin (จะเพิ่มด้านล่าง)
    }

    // CREATE
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(
            @Valid @RequestPart("data") EventUpsertRequest dto,
            @RequestPart(value = "poster",  required = false) MultipartFile poster,
            @RequestPart(value = "detail",  required = false) MultipartFile detail,
            @RequestPart(value = "seatmap", required = false) MultipartFile seatmap
    ) {
        Integer id = service.create(dto, poster, detail, seatmap);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    // UPDATE
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Void> update(
            @PathVariable Integer id,
            @Valid @RequestPart("data") EventUpsertRequest dto,
            @RequestPart(value = "poster",  required = false) MultipartFile poster,
            @RequestPart(value = "detail",  required = false) MultipartFile detail,
            @RequestPart(value = "seatmap", required = false) MultipartFile seatmap
    ) {
        service.update(id, dto, poster, detail, seatmap);
        return ResponseEntity.noContent().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
