package com.example.eventproject.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.eventproject.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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
                            // 0) Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            // 1) Swagger / Docs (ปรับตามที่โปรเจกต์ใช้จริง)
                            .requestMatchers(
                                    "/v3/api-docs/**",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/v2/api-docs",
                                    "/swagger-resources/**",
                                    "/webjars/**"
                            ).permitAll()

                    // 2) Public assets / auth
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/auth/login").permitAll()

                    // 3) Public reads (GET เท่านั้น)
                    .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/zones/session/*/availability").permitAll()

                    // 4) Admin-only
                    .requestMatchers(HttpMethod.GET,"/api/dashboard/summary").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/api/registrations/event/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/events/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")

                    // 5) User-only
                    .requestMatchers("/api/registrations/**").hasRole("USER")

                    // 6) อนุญาต /error
                    .requestMatchers("/error").permitAll()

                    // 7) ที่เหลือ ต้อง authenticated
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
