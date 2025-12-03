package com.project_ant.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret-key}") String secretKey,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.secretKey = secretKey;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String subject) {
        return generateToken(subject, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject + ":" + UUID.randomUUID(), refreshTokenExpirationMs);
    }

    private String generateToken(String subject, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateAndGetSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return null;
        }
    }

    public String validateRefreshTokenAndGetUserKey(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            // subject 형식: "provider:providerId:uuid"
            int lastColonIndex = subject.lastIndexOf(':');
            if (lastColonIndex > 0) {
                return subject.substring(0, lastColonIndex);
            }
            return null;
        } catch (ExpiredJwtException e) {
            log.warn("리프레시 토큰 만료: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.error("리프레시 토큰 검증 실패: {}", e.getMessage());
            return null;
        }
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
