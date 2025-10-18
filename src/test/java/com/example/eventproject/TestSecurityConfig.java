// src/test/java/com/example/eventproject/TestSecurityConfig.java
package com.example.eventproject;

import com.example.eventproject.config.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    OncePerRequestFilter injectCurrentUserFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {

                List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                CurrentUser principal = new CurrentUser(1L, "admin@test.com", "Administrator", roles);

                AbstractAuthenticationToken auth = new AbstractAuthenticationToken(roles) {
                    @Override public Object getCredentials() { return ""; }
                    @Override public Object getPrincipal() { return principal; }
                };
                auth.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(auth);
                chain.doFilter(request, response);
            }
        };
    }

    @Bean
    SecurityFilterChain testChain(HttpSecurity http, OncePerRequestFilter injectCurrentUserFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterAfter(injectCurrentUserFilter, AnonymousAuthenticationFilter.class)
                .build();
    }
}

