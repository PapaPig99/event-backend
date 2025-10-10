package com.example.eventproject.config;

import java.io.IOException;
import java.util.List;          // ปรับแพ็กเกจให้ตรงโปรเจกต์
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.eventproject.model.Role;
import com.example.eventproject.model.User;
import com.example.eventproject.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        final String path = req.getServletPath();
        final String method = req.getMethod();

        // ===== BYPASS เส้นทางที่ไม่ควรตรวจ JWT =====
        if ("OPTIONS".equalsIgnoreCase(method)
                || path.startsWith("/api/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/images/")
                || "/error".equals(path)) {
            chain.doFilter(req, res);
            return;
        }

        // ===== ถ้าเคย Authenticated แล้ว ให้ไปต่อได้เลย =====
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        // ===== อ่าน Authorization header =====
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            // ไม่มี header ก็ปล่อยผ่าน (อย่าเขียน 401 ตรงนี้)
            chain.doFilter(req, res);
            return;
        }

        String token = auth.substring(7);
        String email = null;

        try {
            // คืน email หรือ null ถ้า invalid/expired
            email = jwtUtil.validateAndGetSubject(token);
        } catch (Exception ex) {
            // token ผิดรูป/หมดอายุ → ปล่อยผ่านให้ไปโดน 401/403 ตาม rule ปลายทาง
            chain.doFilter(req, res);
            return;
        }

        if (email != null) {
            Optional<User> userOpt = userRepo.findByEmail(email); // ปรับเมธอดตามจริง
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // สร้างสิทธิ์ โดย normalize ให้เป็น ROLE_XXX เสมอ
                List<SimpleGrantedAuthority> authorities =
                        (user.getRoles() == null || user.getRoles().isEmpty())
                                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                : user.getRoles().stream()
                                      .map(Role::getCode)                               // "ADMIN" หรือ "ROLE_ADMIN"
                                      .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // normalize
                                      .map(SimpleGrantedAuthority::new)
                                      .toList();

                String primaryRole = authorities.get(0).getAuthority().replaceFirst("^ROLE_", "");

                // CurrentUser เป็นคลาสของโปรเจกต์เดิม — ใช้งานต่อได้
                CurrentUser principal = new CurrentUser(user.getId(), user.getEmail(), primaryRole, authorities);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(req, res);
    }
}
