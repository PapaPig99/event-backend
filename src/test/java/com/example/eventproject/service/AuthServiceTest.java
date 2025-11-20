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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock PasswordEncoder encoder;
    @Mock JwtUtil jwt;

    @InjectMocks AuthService service;

    /* =========================
     *        REGISTER
     * ========================= */

    @Test
    @DisplayName("register: สมัครใหม่ปกติ, มี ROLE USER ใน repo → save user + ใช้ role USER ใน JWT")
    void register_newUser_withRole() {
        // arrange
        String emailRaw = " Alice@Example.com ";
        String emailNorm = "alice@example.com";

        // ไม่มี user เดิม
        when(users.findByEmail(emailNorm)).thenReturn(Optional.empty());

        // มี role USER
        Role userRole = new Role();
        userRole.setCode("USER");
        when(roles.findByCode("USER")).thenReturn(Optional.of(userRole));

        when(encoder.encode("p@ss")).thenReturn("bcrypt:p@ss");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.create(emailNorm, "USER")).thenReturn("jwt-token");

        // act
        AuthResponse res = service.register(new RegisterRequest(
                emailRaw,
                "p@ss",
                "Alice"
        ));

        // assert
        assertNotNull(res);
        assertEquals("jwt-token", res.token());
        assertEquals(emailNorm, res.email());
        assertEquals("Alice", res.name());
        assertEquals("USER", res.role());

        verify(users).findByEmail(emailNorm);
        verify(roles).findByCode("USER");
        verify(encoder).encode("p@ss");
        verify(users).save(argThat(u ->
                emailNorm.equals(u.getEmail()) &&
                        "bcrypt:p@ss".equals(u.getPassword()) &&
                        "Alice".equals(u.getName()) &&
                        u.getRole() == userRole
        ));
        verify(jwt).create(emailNorm, "USER");
    }

    @Test
    @DisplayName("register: email เป็น guest ที่เคยมีอยู่แล้ว (password ว่าง) → upgrade เป็นสมาชิกเต็ม")
    void register_upgradeGuest() {
        String email = "guest@example.com";

        User guest = new User();
        guest.setEmail(email);
        guest.setPassword(null);   // guest ยังไม่มี password
        guest.setName("Old Guest");
        guest.setRole(null);       // ยังไม่ได้ role จริง

        when(users.findByEmail(email)).thenReturn(Optional.of(guest));

        Role userRole = new Role();
        userRole.setCode("USER");
        when(roles.findByCode("USER")).thenReturn(Optional.of(userRole));

        when(encoder.encode("newpass")).thenReturn("bcrypt:new");
        when(users.save(guest)).thenReturn(guest);
        when(jwt.create(email, "USER")).thenReturn("jwt-token");

        AuthResponse res = service.register(new RegisterRequest(
                email,
                "newpass",
                "New Name"
        ));

        assertNotNull(res);
        assertEquals("jwt-token", res.token());
        assertEquals(email, res.email());
        assertEquals("New Name", res.name());
        assertEquals("USER", res.role());

        // guest ถูกอัปเดต
        assertEquals("bcrypt:new", guest.getPassword());
        assertEquals("New Name", guest.getName());
        assertEquals(userRole, guest.getRole());

        verify(users).findByEmail(email);
        verify(encoder).encode("newpass");
        verify(users).save(guest);
        verify(jwt).create(email, "USER");
    }

    @Test
    @DisplayName("register: email นี้มี account จริงอยู่แล้ว (password ไม่ว่าง) → IllegalArgumentException")
    void register_existingNonGuest_shouldFail() {
        String email = "existing@example.com";

        User existing = new User();
        existing.setEmail(email);
        existing.setPassword("bcrypt:old"); // แสดงว่าเคยสมัครแล้ว

        when(users.findByEmail(email)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.register(new RegisterRequest(
                        email,
                        "newpass",
                        "Someone"
                ))
        );

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(users).findByEmail(email);
        verify(users, never()).save(any());
        verify(jwt, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("register: ไม่มี ROLE USER ใน repo → ยังสมัครได้, JWT ใช้ roleCode = USER (fallback)")
    void register_newUser_noRoleInRepo() {
        String email = "no-role@example.com";

        when(users.findByEmail(email)).thenReturn(Optional.empty());
        when(roles.findByCode("USER")).thenReturn(Optional.empty()); // ไม่มี role ใน DB

        when(encoder.encode("pw")).thenReturn("bcrypt:pw");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.create(email, "USER")).thenReturn("jwt-token");

        AuthResponse res = service.register(new RegisterRequest(
                email,
                "pw",
                "NoRole"
        ));

        assertNotNull(res);
        assertEquals("jwt-token", res.token());
        assertEquals("USER", res.role());

        // userRole = null → ไม่ควร set role ใน entity
        verify(users).save(argThat(u ->
                email.equals(u.getEmail()) &&
                        "bcrypt:pw".equals(u.getPassword()) &&
                        "NoRole".equals(u.getName()) &&
                        u.getRole() == null
        ));
        verify(jwt).create(email, "USER");
    }

    @Test
    @DisplayName("register: email ว่าง → IllegalArgumentException")
    void register_emptyEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(new RegisterRequest("  ", "pw", "Name")));
        verifyNoInteractions(users, roles, encoder, jwt);
    }

    @Test
    @DisplayName("register: password ว่าง → IllegalArgumentException")
    void register_emptyPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(new RegisterRequest("a@b.com", "  ", "Name")));
        verifyNoInteractions(users, roles, encoder, jwt);
    }

    /* =========================
     *         LOGIN
     * ========================= */

    @Test
    @DisplayName("login: ไม่พบผู้ใช้ → 401 INVALID_CREDENTIALS")
    void login_userNotFound() {
        when(users.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("notfound@example.com", "x"))
        );

        assertEquals(UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("INVALID_CREDENTIALS"));
        verify(jwt, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("login: password เก็บแบบ bcrypt แต่ matches = false → 401")
    void login_bcrypt_wrongPassword() {
        User u = new User();
        u.setEmail("user@example.com");
        u.setPassword("$2b$10$hashhashhash"); // bcrypt รูปแบบใดก็ได้

        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("wrong", "$2b$10$hashhashhash")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("user@example.com", "wrong"))
        );

        assertEquals(UNAUTHORIZED, ex.getStatusCode());
        verify(users, never()).save(any());
        verify(jwt, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("login: password เป็น bcrypt และตรง → ใช้ role จาก user ถ้ามี, แล้วออก JWT")
    void login_bcrypt_ok_withRole() {
        Role admin = new Role();
        admin.setCode("ADMIN");

        User u = new User();
        u.setEmail("admin@example.com");
        u.setPassword("$2y$10$hash");
        u.setRole(admin);

        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("1234", "$2y$10$hash")).thenReturn(true);
        when(jwt.create("admin@example.com", "ADMIN")).thenReturn("jwt-admin");

        AuthResponse res = service.login(new LoginRequest(" ADMIN@example.com ", "1234"));

        assertNotNull(res);
        assertEquals("jwt-admin", res.token());
        assertEquals("ADMIN", res.role());

        // ไม่ต้อง save เพราะเป็น bcrypt อยู่แล้ว
        verify(users, never()).save(any());
        verify(jwt).create("admin@example.com", "ADMIN");
    }

    @Test
    @DisplayName("login: password เก็บเป็น plain text และตรง → upgrade เป็น bcrypt + role fallback USER")
    void login_plain_ok_thenUpgrade() {
        User u = new User();
        u.setEmail("plain@example.com");
        u.setPassword("plain123");
        u.setRole(null); // ไม่มี role -> fallback USER

        when(users.findByEmail("plain@example.com")).thenReturn(Optional.of(u));
        // plain path จะไม่เรียก encoder.matches แต่จะไปเข้าก้อน else
        when(encoder.encode("plain123")).thenReturn("$2a$10$newhash");
        when(jwt.create("plain@example.com", "USER")).thenReturn("jwt-user");

        AuthResponse res = service.login(new LoginRequest("Plain@Example.com", "plain123"));

        assertNotNull(res);
        assertEquals("jwt-user", res.token());
        assertEquals("USER", res.role());

        // ต้องมีการอัปเดตรหัสใน DB
        verify(users).save(argThat(saved ->
                "$2a$10$newhash".equals(saved.getPassword()) &&
                        "plain@example.com".equals(saved.getEmail())
        ));

        // ใน path นี้ไม่ควรเรียก matches
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login: password plain แต่ไม่ตรง → 401")
    void login_plain_wrongPassword() {
        User u = new User();
        u.setEmail("plain2@example.com");
        u.setPassword("abcd"); // not bcrypt

        when(users.findByEmail("plain2@example.com")).thenReturn(Optional.of(u));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("plain2@example.com", "xyz"))
        );

        assertEquals(UNAUTHORIZED, ex.getStatusCode());
        verify(users, never()).save(any());
        verify(jwt, never()).create(anyString(), anyString());
    }
}
