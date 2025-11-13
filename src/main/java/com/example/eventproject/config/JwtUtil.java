package com.example.eventproject.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "super-secret-key-change-me-please-32bytes-minimum";
    private static final long EXP_MS = 1000L * 60 * 60 * 24;

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String subjectEmail, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXP_MS);
        return Jwts.builder()
                .setSubject(subjectEmail)
                .claim("email", subjectEmail)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // alias ให้เข้ากับโค้ดเดิมใน AuthService
    public String create(String subjectEmail, String role) {
        return generateToken(subjectEmail, role);
    }

    public String validateAndGetSubject(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
