package com.example.eventproject.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.eventproject.dto.EventDetailDto;
import com.example.eventproject.dto.EventSummaryView;
import com.example.eventproject.dto.EventUpsertDto;
import com.example.eventproject.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    

    @Operation(summary = "รายการ Event", description = "คืนรายการแบบสรุป พร้อมตัวกรอง")
    @GetMapping
    public List<EventSummaryView> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "start_date,asc") String sort
    ) {
        // TODO: ส่งต่อพารามิเตอร์เข้าชั้น service
        return service.list();
    }

    @Operation(summary = "รายละเอียด Event (รวม sessions, zones)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบ id")
    })
    @GetMapping("/{id}")
    public EventDetailDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @Operation(summary = "สร้าง Event", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างสำเร็จ (มี Location header)"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ผ่าน validation")
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> create(@Valid @RequestBody EventUpsertDto dto) {
        Integer id = service.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "แก้ไข Event ตาม id", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value="/{id}", consumes = "application/json")
    public ResponseEntity<Void> update(@PathVariable Integer id,
                                       @Valid @RequestBody EventUpsertDto dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "ลบ Event ตาม id", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
