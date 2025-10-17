package com.example.eventproject.controller;

import com.example.eventproject.config.CurrentUser;
import com.example.eventproject.dto.RegistrationDto;
import com.example.eventproject.model.Registration;
import com.example.eventproject.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    // สร้างการจองใหม่ (PENDING/HOLD)
    @PostMapping
    public ResponseEntity<RegistrationDto.Response> create(
            @RequestBody RegistrationDto.CreateRequest req,
            @AuthenticationPrincipal CurrentUser user
    ) {
        Registration reg = registrationService.create(req, user.getId().intValue());
        return ResponseEntity.status(201).body(RegistrationDto.Response.from(reg));
    }

    // ยืนยันการจ่ายเงิน (ต้องส่ง id ของ registration)
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<RegistrationDto.Response> confirm(
            @PathVariable Integer id,
            @RequestBody RegistrationDto.ConfirmRequest req,
            @AuthenticationPrincipal CurrentUser user
    ) {
        // ส่ง id เข้า service ด้วย
        Registration reg = registrationService.confirm(id, req, user.getId().intValue());
        return ResponseEntity.ok(RegistrationDto.Response.from(reg));
    }

    // ยกเลิกการจอง (ต้องส่ง id ของ registration)
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RegistrationDto.Response> cancel(
            @PathVariable Integer id,
            @RequestBody(required = false) RegistrationDto.CancelRequest req,
            @AuthenticationPrincipal CurrentUser user
    ) {
        // ส่ง id เข้า service ด้วย
        Registration reg = registrationService.cancel(id, req, user.getId().intValue());
        return ResponseEntity.ok(RegistrationDto.Response.from(reg));
    }

    // ดึงรายการจองของตัวเองทุก event
    @GetMapping("/me")
    public ResponseEntity<?> myRegs(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                registrationService.getByUserId(user.getId().intValue(), status)
        );
    }
    // ดึงรายการจองของทุกuserที่จ่ายตังแล้ว ตามevent
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RegistrationDto.Response>> getAllByEvent(
            @PathVariable Integer eventId,
            @RequestParam(required = false) String status
    ) {
        var result = registrationService.getAllByEvent(eventId, status);
        return ResponseEntity.ok(result);
    }
    // ดึงรายการจองของทุก user ที่จ่ายเงินแล้ว ตาม event + session
    @GetMapping("/event/{eventId}/{sessionId}")
    public ResponseEntity<List<RegistrationDto.Response>> getAllByEventAndSession(
            @PathVariable Integer eventId,
            @PathVariable Integer sessionId,
            @RequestParam(required = false) String status
    ) {
        var result = registrationService.getAllByEventAndSession(eventId, sessionId, status);
        return ResponseEntity.ok(result);
    }


}
