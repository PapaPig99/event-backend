package com.example.eventproject.service;

import com.example.eventproject.config.JwtUtil;
import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.RoleRepository;
import com.example.eventproject.repository.UserRepository;
import com.example.eventproject.dto.RegisterRequest;
import com.example.eventproject.dto.LoginRequest;
import com.example.eventproject.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// ✅ เพิ่ม import สองบรรทัดนี้
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Optional;
import java.util.Set;

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
        String email = req.email().trim().toLowerCase();
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
        String email = req.email().trim().toLowerCase();
        Optional<User> userOpt = users.findByEmail(email);

        // ❗เปลี่ยนเป็น 401 แทน 500
        User u = userOpt.orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS"));

        String raw = req.password();
        String stored = u.getPassword();
        boolean isBcrypt = stored != null && stored.startsWith("$2"); // $2a/$2b/$2y

        // ✅ รองรับทั้ง bcrypt และ plain (กรณี admin ที่ถูก INSERT ตรง ๆ)
        boolean ok = isBcrypt
                ? encoder.matches(raw, stored)
                : raw != null && raw.equals(stored);

        if (!ok) {
            throw new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        // 🔁 ถ้าเป็น plain และล็อกอินผ่าน → แฮชแล้วบันทึกกลับ (auto-migrate)
        if (!isBcrypt) {
            u.setPassword(encoder.encode(stored));
            users.save(u);
        }

        String role = "USER";
        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            role = u.getRoles().stream().findFirst().map(Role::getCode).orElse("USER");
        }

        String token = jwt.create(u.getEmail(), role);
        return new AuthResponse(token, u.getId(), u.getEmail(), u.getName(), role);
    }
}
