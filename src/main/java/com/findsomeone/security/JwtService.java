package com.findsomeone.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationTime;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms:3600000}") long expirationTime) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "APP_JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        if (expirationTime <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    public long getExpirationTimeSeconds() {
        return expirationTime / 1000;
    }

    public String generateToken(Long userId) {

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public Long extractUserId(String token) {

        String userId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.parseLong(userId);
    }

    public boolean isTokenValid(String token) {

        try {
            extractUserId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
