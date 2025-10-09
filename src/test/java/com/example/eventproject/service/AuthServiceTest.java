package com.example.eventproject.service;

import com.example.eventproject.config.JwtUtil;
import com.example.eventproject.dto.AuthResponse;
import com.example.eventproject.dto.LoginRequest;
import com.example.eventproject.dto.RegisterRequest;
import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.RoleRepository;
import com.example.eventproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository users;
    @Mock private RoleRepository roles;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtUtil jwt;

    @InjectMocks private AuthService service;

    /* =========================
     *        REGISTER
     * ========================= */

    @Test
    @DisplayName("register: สำเร็จ (มี ROLE USER จาก repo) -> save ผู้ใช้ + สร้าง JWT ด้วย role จริง")
    void register_ok_with_role() {
        // Arrange
        String emailRaw = " Alice@Example.com ";
        String email = "alice@example.com";
        when(users.existsByEmail(email)).thenReturn(false);

        Role userRole = new Role();
        userRole.setCode("USER");
        when(roles.findByCode("USER")).thenReturn(Optional.of(userRole));

        when(encoder.encode("p@ss")).thenReturn("bcrypt:xxx");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L); // จำลองว่า DB ใส่ id ให้
            return u;
        });
        when(jwt.create(email, "USER")).thenReturn("jwt-token");

        // Act
        AuthResponse res = service.register(new RegisterRequest(
                emailRaw, "p@ss", "Alice", "0800000000", "ACME"
        ));

        // Assert — ไม่เจาะ DTO ภายในมาก แต่อย่างน้อยต้องไม่ null และไม่ throw
        assertNotNull(res);

        // ตรวจว่าถูก normalize email และเรียกตามลำดับที่ถูกต้อง
        verify(users).existsByEmail(email);
        verify(roles).findByCode("USER");
        verify(encoder).encode("p@ss");
        verify(users).save(argThat(u ->
                email.equals(u.getEmail()) &&
                        "bcrypt:xxx".equals(u.getPassword()) &&
                        "Alice".equals(u.getName()) &&
                        "0800000000".equals(u.getPhone()) &&
                        "ACME".equals(u.getOrganization()) &&
                        u.getRoles().contains(userRole)
        ));
        verify(jwt).create(email, "USER");
    }

    @Test
    @DisplayName("register: Email ซ้ำ -> IllegalArgumentException")
    void register_email_duplicated() {
        String email = "bob@example.com";
        when(users.existsByEmail(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.register(new RegisterRequest(email, "x", "Bob", null, null))
        );
        verify(users, never()).save(any());
        verify(jwt, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("register: ไม่มี ROLE USER ใน repo -> กำหนด roleCode=USER ใน JWT และไม่ set roles ให้ user")
    void register_no_role_in_repo() {
        String email = "cindy@example.com";
        when(users.existsByEmail(email)).thenReturn(false);
        when(roles.findByCode("USER")).thenReturn(Optional.empty());

        when(encoder.encode("pw")).thenReturn("bcrypt:pw");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(9L);
            return u;
        });
        when(jwt.create(email, "USER")).thenReturn("jwt"); // roleCode fallback

        AuthResponse res = service.register(new RegisterRequest(email, "pw", "Cindy", null, null));
        assertNotNull(res);

        // ไม่มี roles ถูก set (เพราะ userRole=null)
        verify(users).save(argThat(u -> u.getRoles() == null || u.getRoles().isEmpty()));
        verify(jwt).create(email, "USER");
    }

    /* =========================
     *         LOGIN
     * ========================= */

    @Test
    @DisplayName("login: ไม่พบผู้ใช้ -> 401 ResponseStatusException")
    void login_user_not_found_401() {
        when(users.findByEmail("dan@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.login(new LoginRequest("dan@example.com", "x"))
        );
        assertEquals(UNAUTHORIZED, ex.getStatusCode());
        verify(jwt, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("login: พบผู้ใช้ แต่รหัสผ่าน (bcrypt) ไม่ตรง -> 401")
    void login_wrong_password_bcrypt_401() {
        User u = new User();
        u.setEmail("ed@example.com");
        u.setPassword("$2b$10$abcdef"); // สมมุติเป็น bcrypt
        when(users.findByEmail("ed@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("wrong", "$2b$10$abcdef")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.login(new LoginRequest("ed@example.com", "wrong"))
        );
        assertEquals(UNAUTHORIZED, ex.getStatusCode());
        verify(users, never()).save(any(User.class)); // ไม่ควรอัปเดตรหัสผ่าน
        verify(jwt, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("login: bcrypt ถูกต้อง -> ออก JWT ด้วย role แรกของผู้ใช้")
    void login_ok_bcrypt() {
        Role admin = new Role(); admin.setCode("ADMIN");
        User u = new User();
        u.setEmail("fin@example.com");
        u.setPassword("$2y$10$hash");
        u.setRoles(Set.of(admin));

        when(users.findByEmail("fin@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("1234", "$2y$10$hash")).thenReturn(true);
        when(jwt.create("fin@example.com", "ADMIN")).thenReturn("jwt-admin");

        AuthResponse res = service.login(new LoginRequest(" fin@example.com ", "1234"));
        assertNotNull(res);

        // ไม่ต้องอัปเกรดรหัสผ่าน เพราะเป็น bcrypt อยู่แล้ว
        verify(users, never()).save(any(User.class));
        verify(jwt).create("fin@example.com", "ADMIN");
    }

    @Test
    @DisplayName("login: รหัสเป็น plain แล้วตรง -> ออก JWT และอัปเกรดรหัสเป็น bcrypt (auto-migrate)")
    void login_ok_plain_then_upgrade() {
        User u = new User();
        u.setEmail("gina@example.com");
        u.setPassword("plain123"); // ไม่ใช่ bcrypt
        // ไม่มี roles → ควร fallback เป็น USER
        when(users.findByEmail("gina@example.com")).thenReturn(Optional.of(u));

        // plain path: service จะ compare raw กับ stored โดยตรง → ไม่เรียก matches()
        when(encoder.encode("plain123")).thenReturn("$2a$10$newhash"); // ใช้ตอนอัปเกรด

        when(jwt.create("gina@example.com", "USER")).thenReturn("jwt-user");

        AuthResponse res = service.login(new LoginRequest("GINA@example.com", "plain123"));
        assertNotNull(res);

        // ต้องมีการอัปเดตรหัสผ่านกลับ DB
        verify(users).save(argThat(saved ->
                "$2a$10$newhash".equals(saved.getPassword()) &&
                        "gina@example.com".equals(saved.getEmail())
        ));
        verify(jwt).create("gina@example.com", "USER");
        // ไม่ควรเรียก encoder.matches ในกรณี plain
        verify(encoder, never()).matches(anyString(), anyString());
    }
}
