package com.example.eventproject.service;

import java.util.Optional;
import java.util.Set;

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

    public AuthResponse register(RegisterRequest req) {
        String email = (req.email() == null ? "" : req.email()).trim().toLowerCase();
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

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

        return new AuthResponse(token, u.getId(), u.getEmail(), u.getName(), roleCode);
    }

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

        // เลือก role แรก ถ้าไม่มีให้เป็น USER (normalize ไม่ต้องมี ROLE_ ก็ได้ เพราะ JwtAuthFilter จะเติมให้)
        String roleCode = "USER";
        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            roleCode = u.getRoles().stream().findFirst().map(Role::getCode).orElse("USER");
        }

        String token = jwt.create(u.getEmail(), roleCode);
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getName(), roleCode);
    }
}
