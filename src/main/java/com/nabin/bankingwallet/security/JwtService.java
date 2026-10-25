package com.nabin.bankingwallet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    @Getter
    private final long expirationSeconds;


    public JwtService(
            @Value("${app.jwt.secret}")
            String secret,
            @Value
                    ("${app.jwt.expiration-seconds}")
            long expirationSeconds) {

        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserPrincipal userPrincipal) {
        long now = System.currentTimeMillis();
        // Implementation for generating JWT token
        return Jwts
                .builder()
                .subject(userPrincipal.getEmail())
                .claim("role", userPrincipal
                        .getUserRole()
                        .name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationSeconds * 1000))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token)
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiration(String token) {
        Date expiration = parse(token).getExpiration();

        return Math.max(
                0,
                (expiration.getTime() - System.currentTimeMillis()) / 1000
                       );
    }

    public Claims parse(String token) {
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
