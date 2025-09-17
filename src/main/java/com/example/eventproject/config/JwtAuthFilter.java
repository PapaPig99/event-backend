package com.example.eventproject.config;

import com.example.eventproject.repository.UserRepository;
import com.example.eventproject.model.User; // ปรับแพ็กเกจให้ตรงโปรเจกต์

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            String email = jwtUtil.validateAndGetSubject(token); // คืน email หรือ null ถ้า invalid/expired

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOpt = userRepo.findByEmail(email); // ปรับเมธอดตามจริง
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // ถ้าไม่มี role ก็ให้สิทธิ์ว่าง ๆ ไปก่อน (ไม่พึ่ง user.getAuthorities())
                    String role = safeRole(user); // เช่น "USER"
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    // ใช้ principal เป็น email ก็พอ ไม่ต้องเป็น entity ทั้งตัว
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        chain.doFilter(req, res);
    }

    private String safeRole(User user) {
        try {
            // ถ้า User มี field ชื่อ role (String) เช่น "USER" / "ADMIN"
            String r = (String) User.class.getMethod("getRole").invoke(user);
            return (r != null && !r.isBlank()) ? r : "USER";
        } catch (Exception ignore) {
            return "USER";
        }
    }
}
