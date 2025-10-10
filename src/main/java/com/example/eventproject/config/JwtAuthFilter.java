package com.example.eventproject.config;

import com.example.eventproject.model.Role;
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
        System.out.println("[JWT] HIT " + req.getMethod() + " " + req.getRequestURI());
        System.out.println("[JWT] Authorization: " + req.getHeader("Authorization"));

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            String email = jwtUtil.validateAndGetSubject(token); // คืน email หรือ null ถ้า invalid/expired

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOpt = userRepo.findByEmail(email); // ปรับเมธอดตามจริง
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    var authorities = user.getRoles().isEmpty()
                            ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            : user.getRoles().stream()
                            .map(Role::getCode)                               // "ADMIN" หรือ "ROLE_ADMIN"
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // normalize
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    String primaryRole = authorities.get(0).getAuthority().replaceFirst("^ROLE_", "");

                    var principal = new CurrentUser(user.getId(), user.getEmail(), primaryRole, authorities);
                    var authToken  = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                }
            }
        }
        chain.doFilter(req, res);
    }
}
