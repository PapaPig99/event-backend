package com.example.eventproject.config;

import com.example.eventproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // ===== CORS =====
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "http://localhost:8081",   // frontend on nginx container (prod in compose)
                "http://127.0.0.1:8081",
                "http://localhost:5173",   // vite dev
                "http://127.0.0.1:5173"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    // ===== Security rules =====
    @Bean
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ เปิด dashboard (สำหรับหน้า Overview) ให้เข้าถึงได้โดยไม่ต้องล็อกอิน
                .requestMatchers("/api/dashboard/**").permitAll()

                // Public auth & assets
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/images/**").permitAll()

                // Public: list events
                .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/").permitAll()

                // Protected: event details & mutations
                .requestMatchers(HttpMethod.GET,    "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.PATCH,  "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").authenticated()

                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil, userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
