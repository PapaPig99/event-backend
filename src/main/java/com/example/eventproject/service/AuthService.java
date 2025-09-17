package com.example.eventproject.service;

import com.example.eventproject.config.JwtUtil;
import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.RoleRepository;
import com.example.eventproject.repository.UserRepository;
import com.example.eventproject.dto.RegisterRequest; // << ใช้ DTO จาก package dto
import com.example.eventproject.dto.LoginRequest;    // << เช่นกัน
import com.example.eventproject.dto.AuthResponse;    // ถ้าคุณมี AuthResponse อยู่ใน dto
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;   // ต้องมี ถ้าใช้ตาราง roles
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthService(UserRepository users, RoleRepository roles,
                       PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // เปลี่ยนให้รับ dto.RegisterRequest โดยตรง และ "ไม่ต้อง" ส่ง Role มาจาก Controller
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        // หา role USER (ถ้าไม่เจอ ให้ fallback เป็น USER แบบไม่มี entity)
        Role userRole = roles.findByCode("USER").orElse(null);

        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setName(req.name());
        u.setPhone(req.phone());
        u.setOrganization(req.organization());
        if (userRole != null) {
            u.setRoles(Set.of(userRole));
        }

        users.save(u);

        String roleCode = userRole != null ? userRole.getCode() : "USER";
        String token = jwt.create(u.getEmail(), roleCode);

        // ถ้า AuthResponse อยู่ใน dto ให้ new ตามคอนสตรัคเตอร์ของคุณ
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getName(), roleCode);
    }

    // เปลี่ยนให้รับ dto.LoginRequest โดยตรง
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        Optional<User> userOpt = users.findByEmail(email);
        User u = userOpt.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String role = "USER";
        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            role = u.getRoles().stream().findFirst().map(Role::getCode).orElse("USER");
        }

        String token = jwt.create(u.getEmail(), role);
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getName(), role);
    }
}
