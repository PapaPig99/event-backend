package com.example.eventproject.controller;

import java.net.URI;
import java.util.List;

import com.example.eventproject.config.CurrentUser;
import com.example.eventproject.dto.EventDetailViewDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping(value = "/api/events", produces = "application/json")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    /* =========================
     *           READ
     * ========================= */

    //summary รายการ Event
    @GetMapping
    public List<EventSummaryView> list() {
        return service.list();
    }

    //รายละเอียด Event (รวม sessions และ zones)
    @GetMapping("/{id}")
    public EventDetailDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    //รายละเอียด Event (View: รวม sessions + สถานะขาย + ราคาบัตรทุกโซน)
    @GetMapping("/{id}/view")
    public EventDetailViewDto getView(@PathVariable Integer id) {
        return service.getView(id);
    }



    /* =========================
     *         CREATE
     * ========================= */

    // สร้าง Event
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(
            @Valid @RequestPart("data") EventUpsertRequest dto,
            @RequestPart(value = "poster",  required = false) MultipartFile poster,
            @RequestPart(value = "seatmap", required = false) MultipartFile seatmap,
            @AuthenticationPrincipal CurrentUser user
    ) {
        // ส่ง userId ไปยัง service
        Integer id = service.create(dto, poster, seatmap, user.getEmail());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();

        return ResponseEntity.created(location).build();
    }


    /* =========================
     *          UPDATE
     * ========================= */

    //แก้ไข Event ตาม id
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Void> update(
            @PathVariable Integer id,
            @Valid @RequestPart("data") EventUpsertRequest dto,
            @RequestPart(value = "poster",  required = false) MultipartFile poster,
            @RequestPart(value = "seatmap", required = false) MultipartFile seatmap
    ) {
        service.update(id, dto, poster, seatmap);
        return ResponseEntity.noContent().build();
    }

    /* =========================
     *          DELETE
     * ========================= */

    //ลบ Event ตาม id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
