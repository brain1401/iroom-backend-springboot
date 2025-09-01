package com.iroomclass.springbackend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 유틸리티 클래스
 * 
 * <p>JWT 토큰의 생성, 파싱, 검증 기능을 제공합니다.
 * HMAC-SHA512 알고리즘을 사용하여 토큰의 무결성을 보장합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
@Slf4j
public class JwtUtil {
    
    /**
     * JWT 비밀키 (환경변수에서 설정, 기본값 제공)
     */
    @Value("${jwt.secret:iroom-jwt-secret-key-for-development-only-change-in-production-2025}")
    private String secretKey;
    
    /**
     * JWT 만료시간 (기본 24시간)
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    /**
     * 서명에 사용할 SecretKey
     */
    private SecretKey key;
    
    /**
     * 빈 초기화 후 SecretKey 생성
     */
    @PostConstruct
    public void init() {
        // 비밀키가 충분히 길지 않은 경우 경고 로그
        if (secretKey.length() < 64) {
            log.warn("현재 JWT 비밀키가 64바이트보다 짧습니다. 운영 환경에서는 더 긴 비밀키를 사용하세요.");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    /**
     * JWT 토큰 생성
     * 
     * @param username 사용자명
     * @param userId 사용자 ID
     * @param role 사용자 역할
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String username, UUID userId, String role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }
    
    /**
     * JWT 토큰에서 사용자명 추출
     * 
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public UUID getUserIdFromToken(String token) {
        String userIdStr = getClaimsFromToken(token).get("userId", String.class);
        return UUID.fromString(userIdStr);
    }
    
    /**
     * JWT 토큰에서 사용자 역할 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 역할
     */
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }
    
    /**
     * JWT 토큰에서 만료시간 추출
     * 
     * @param token JWT 토큰
     * @return 만료시간
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }
    
    /**
     * JWT 토큰에서 Claims 추출
     * 
     * @param token JWT 토큰
     * @return JWT Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * JWT 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWT 토큰 만료 여부 확인
     * 
     * @param token JWT 토큰
     * @return 만료되었으면 true, 그렇지 않으면 false
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * HTTP Authorization 헤더에서 Bearer 토큰 추출
     * 
     * @param authHeader Authorization 헤더 값
     * @return JWT 토큰 또는 null
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}