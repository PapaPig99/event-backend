package com.example.eventproject.service;

import java.util.Optional;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.eventproject.config.JwtUtil;
import com.example.eventproject.dto.AuthResponse;
import com.example.eventproject.dto.LoginRequest;
import com.example.eventproject.dto.RegisterRequest;
import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.RoleRepository;
import com.example.eventproject.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthService(UserRepository users, RoleRepository roles,
                       PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /* ==========================================================
       REGISTER — สมัครสมาชิกใหม่ / อัปเกรด guest
       ========================================================== */
    public AuthResponse register(RegisterRequest req) {
        String email = (req.email() == null ? "" : req.email()).trim().toLowerCase();
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String rawPassword = req.password();
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        String name = req.name();

        // หา role USER จากตาราง roles
        Role userRole = roles.findByCode("USER").orElse(null);

        // ===== ถ้ามี user อยู่แล้ว ให้เช็คว่าเป็น guest รึเปล่า =====
        Optional<User> existingOpt = users.findByEmail(email);

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            String stored = existing.getPassword();
            boolean hasRealPassword = (stored != null && !stored.isBlank());

            // ถ้ามี password อยู่แล้ว แสดงว่าเป็น account ที่สมัครแล้ว → ห้ามสมัครซ้ำ
            if (hasRealPassword) {
                throw new IllegalArgumentException("Email already in use");
            }

            // กรณีนี้คือ guest user (เคยจองตั๋วด้วย email นี้มาก่อน) → อัปเกรดเป็นสมาชิกเต็มตัว
            existing.setPassword(encoder.encode(rawPassword));
            existing.setName(name);

            if (existing.getRole() == null && userRole != null) {
                existing.setRole(userRole);
            }

            users.save(existing);

            String roleCode = (existing.getRole() != null && existing.getRole().getCode() != null)
                    ? existing.getRole().getCode()
                    : "USER";

            String token = jwt.create(existing.getEmail(), roleCode);
            return new AuthResponse(token, existing.getEmail(), existing.getName(), roleCode);
        }

        // ===== กรณีไม่เคยมี user มาก่อน → สมัครใหม่ปกติ =====
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setName(name);
        if (userRole != null) {
            u.setRole(userRole);
        }

        users.save(u);

        String roleCode = (userRole != null && userRole.getCode() != null)
                ? userRole.getCode()
                : "USER";

        String token = jwt.create(u.getEmail(), roleCode);

        return new AuthResponse(token, u.getEmail(), u.getName(), roleCode);
    }

    /* ==========================================================
       LOGIN — เข้าสู่ระบบ
       ========================================================== */
    public AuthResponse login(LoginRequest req) {
        String email = (req.email() == null ? "" : req.email()).trim().toLowerCase();
        String raw = req.password() == null ? "" : req.password();

        Optional<User> userOpt = users.findByEmail(email);
        User u = userOpt.orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS"));

        String stored = u.getPassword() == null ? "" : u.getPassword();

        // ตรวจว่าเป็น BCrypt จริง ๆ ($2a/$2b/$2y)
        boolean isBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");

        boolean ok;
        if (isBcrypt) {
            ok = encoder.matches(raw, stored);
        } else {
            // legacy: plaintext ใน DB → เทียบตรง 1 ครั้ง
            ok = !stored.isEmpty() && stored.equals(raw);
            // ถ้าเทียบตรงผ่าน → อัปเกรดเป็น BCrypt ทันที
            if (ok) {
                u.setPassword(encoder.encode(raw)); // encode จากรหัสที่กรอก
                users.save(u);
            }
        }

        if (!ok) {
            throw new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        // ===== เลือก role จาก user ถ้าไม่มีให้เป็น USER =====
        String roleCode = "USER";
        if (u.getRole() != null && u.getRole().getCode() != null) {
            roleCode = u.getRole().getCode();
        }

        String token = jwt.create(u.getEmail(), roleCode);
        return new AuthResponse(token, u.getEmail(), u.getName(), roleCode);
    }
}
