package com.example.eventproject.controller;

import com.example.eventproject.dto.EventUpsertDto;
import com.example.eventproject.dto.EventDetailDto;
import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import lombok.RequiredArgsConstructor;


import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Events", description = "CRUD สำหรับ Event")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @Operation(summary = "รายการ Event", description = "คืนรายการแบบสรุป")
    @GetMapping
    public List<EventSummaryView> list() {
        return service.list();
    }

    @Operation(summary = "รายละเอียด Event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบ id")
    })
    @GetMapping("/{id}")
    public EventDetailDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @Operation(summary = "สร้าง Event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างสำเร็จ (มี Location header)"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ผ่าน validation")
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> create(@Valid @RequestBody EventUpsertDto dto) {
        Integer id = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "แก้ไข Event ตาม id")
    @PutMapping(value="/{id}", consumes = "application/json")
    public ResponseEntity<Void> update(@PathVariable Integer id,
                                       @Valid @RequestBody EventUpsertDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ลบ Event ตาม id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
