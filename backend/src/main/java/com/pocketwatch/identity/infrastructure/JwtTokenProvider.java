package com.pocketwatch.identity.infrastructure;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Token Provider
 * 
 * Responsible for:
 * - Generating internal JWT tokens after Google OIDC verification
 * - Validating JWT tokens
 * - Extracting claims from JWT
 * 
 * CRITICAL SECURITY:
 * - JWT is signed with a strong secret key
 * - Token expiration is finite
 * - Token contains only necessary claims (userId, email, issued at, expiration)
 * - No Google tokens or sensitive data in JWT payload
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    /**
     * Generate JWT token for authenticated user
     * 
     * @param userId User ID from database
     * @param email User's email
     * @return Signed JWT token
     */
    public String generateToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusMillis(jwtExpiration);
        
        SecretKey key = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
        
        String token = Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expirationTime))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
        
        log.debug("Generated JWT token for user: {}", userId);
        return token;
    }
    
    /**
     * Validate JWT token
     * 
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
            );
            
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract user ID from JWT token
     * 
     * @param token JWT token
     * @return User ID (subject claim)
     */
    public String getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
        
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Extract email from JWT token
     * 
     * @param token JWT token
     * @return Email address
     */
    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
        
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        return claims.get("email", String.class);
    }
}
