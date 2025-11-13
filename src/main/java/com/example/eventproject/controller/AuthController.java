package com.example.eventproject.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.eventproject.config.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.eventproject.dto.AuthResponse;
import com.example.eventproject.dto.LoginRequest;
import com.example.eventproject.dto.RegisterRequest;
import com.example.eventproject.repository.UserRepository;
import com.example.eventproject.service.AuthService;

@Tag(name = "Auth", description = "Authentication & Users")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final UserRepository userRepository;

    public AuthController(AuthService auth, UserRepository userRepository) {
        this.auth = auth;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Register")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(auth.register(req));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

     /* ==========================
       GET CURRENT USER PROFILE
       ========================== */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        // ไม่มี token หรือ token ใช้ไม่ได้ → ไม่มี user
        if (currentUser == null) {
            return ResponseEntity.status(404).body("No user");
        }

        // email หลักมาจาก principal ใน SecurityContext
        String email = currentUser.getEmail();

        var user = userRepository.findById(email).orElse(null);

        return ResponseEntity.ok(
                Map.of(
                        "name", user != null && user.getName() != null && !user.getName().isBlank()
                                ? user.getName()
                                : currentUser.getName(),    // เผื่อใน DB ยังไม่มีชื่อ
                        "email", email
                )
        );
    }


}


