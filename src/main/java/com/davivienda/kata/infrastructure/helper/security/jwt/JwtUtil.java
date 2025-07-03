package com.davivienda.kata.infrastructure.helper.security.jwt;

import com.davivienda.kata.domain.model.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final String secretKey = "12345678901234567890123456789012";

    public String generateToken(UserEntity user) {
        return Jwts.builder()
                .subject(user.getUser())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser() // 👈 nuevo método estático en 0.12.x
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    public boolean isTokenValid(String token) {
        try {
            getUsernameFromToken(token); // validación de firma y expiración
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
