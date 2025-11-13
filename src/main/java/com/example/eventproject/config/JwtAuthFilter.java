package com.example.eventproject.config;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

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
                                    FilterChain chain)
            throws ServletException, IOException {

        final String path = req.getServletPath();
        final String method = req.getMethod();

        // ===== BYPASS เส้นทางที่ไม่ต้องตรวจ JWT =====
        if ("OPTIONS".equalsIgnoreCase(method)
                // อนุญาต register / login ไม่ต้องมี token
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/images/")
                || "/error".equals(path)) {
            chain.doFilter(req, res);
            return;
        }

        // ===== ถ้ามี Authentication อยู่แล้ว ให้ไปต่อเลย =====
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        // ===== อ่าน Authorization header =====
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(7);
        String email;

        try {
            email = jwtUtil.validateAndGetSubject(token);
        } catch (Exception ex) {
            chain.doFilter(req, res);
            return;
        }

        if (email != null) {
            Optional<User> userOpt = userRepo.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // normalize role เป็น ROLE_XXX
                String roleCode = (user.getRole() != null)
                        ? user.getRole().getCode()
                        : "USER";

                String normalizedRole = roleCode.startsWith("ROLE_")
                        ? roleCode
                        : "ROLE_" + roleCode;

                var authorities = List.of(new SimpleGrantedAuthority(normalizedRole));
                String primaryRole = normalizedRole.replaceFirst("^ROLE_", "");

                // ใช้ email แทน id
                CurrentUser principal = new CurrentUser(
                        user.getEmail(),
                        user.getName(),
                        primaryRole,
                        authorities
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(req, res);
    }
}
