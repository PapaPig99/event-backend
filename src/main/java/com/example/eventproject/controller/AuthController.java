package com.example.eventproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Authentication & Users")
@RestController
@RequestMapping("/api")
public class AuthController {

    @Operation(summary = "Login และรับ JWT")
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Object body) {
        // TODO: implement
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Logout (ทำให้ token ใช้ไม่ได้)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "สมัครผู้ใช้ใหม่")
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Object body) {
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "ข้อมูลผู้ใช้ที่ล็อกอิน", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.ok().build();
    }
}
