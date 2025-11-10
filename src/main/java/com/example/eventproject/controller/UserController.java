package com.example.eventproject.controller;

import com.example.eventproject.model.User;
import com.example.eventproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /* ดึงผู้ใช้ทั้งหมด */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /* ดึงผู้ใช้ตาม role เช่น /api/users/role/ADMIN */
    @GetMapping("/role/{roleCode}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String roleCode) {
        try {
            return ResponseEntity.ok(userService.getUsersByRoleCode(roleCode));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* ดึง guest ทั้งหมด */
    @GetMapping("/guests")
    public ResponseEntity<List<User>> getGuestUsers() {
        return ResponseEntity.ok(userService.getAllGuests());
    }
}
