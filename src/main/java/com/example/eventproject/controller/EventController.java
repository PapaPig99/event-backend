package com.example.eventproject.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.eventproject.dto.EventDetailDto;
import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.dto.EventUpsertRequest;
import com.example.eventproject.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Events", description = "CRUD สำหรับ Event")
@RestController
@RequestMapping(value = "/api/events", produces = "application/json")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    /* =========================
     *           READ
     * ========================= */

    @Operation(summary = "รายการ Event")
    @GetMapping
    public List<EventSummaryView> list() {
        return service.list();
    }

    @Operation(summary = "รายละเอียด Event (รวม sessions และ zones)")
    @ApiResponse(responseCode = "200", description = "สำเร็จ")
    @GetMapping("/{id}")
    public EventDetailDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    /* =========================
     *         CREATE
     * ========================= */

    @Operation(summary = "สร้าง Event", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "สร้างสำเร็จ (มี Location header)")
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

    /* =========================
     *          UPDATE
     * ========================= */

    @Operation(summary = "แก้ไข Event ตาม id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "แก้ไขสำเร็จ")
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

    /* =========================
     *          DELETE
     * ========================= */

    @Operation(summary = "ลบ Event ตาม id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "ลบสำเร็จ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
