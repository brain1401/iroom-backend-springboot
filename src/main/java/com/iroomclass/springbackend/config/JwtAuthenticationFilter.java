package com.iroomclass.springbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * 
 * <p>모든 HTTP 요청에 대해 JWT 토큰을 검증하고, 
 * 유효한 토큰이 있는 경우 SecurityContext에 인증 정보를 설정합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    /**
     * JWT 토큰 검증 및 인증 설정
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            // 토큰이 없거나 이미 인증된 경우 스키프
            if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // JWT 토큰 유효성 검증
            if (jwtUtil.validateToken(token)) {
                // 토큰에서 사용자 정보 추출
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                // 인증 객체 생성
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("사용자 [{}] JWT 인증 성공, 역할: {}", username, role);
            } else {
                log.debug("JWT 토큰 유효성 검증 실패");
            }
            
        } catch (Exception e) {
            log.warn("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
        }
        
        // 다음 필터로 전달
        filterChain.doFilter(request, response);
    }
    
    /**
     * 필터를 적용하지 않을 경로 설정
     * Swagger, 헬스체크 등은 JWT 검증에서 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        
        // Swagger 관련 경로
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||
            path.startsWith("/api/swagger-ui") || path.startsWith("/api/v3/api-docs")) {
            return true;
        }
        
        // 시스템 헬스체크
        if (path.equals("/api/system/health") || path.equals("/system/health")) {
            return true;
        }
        
        // 로그인 API
        if (path.equals("/api/auth/login") || path.equals("/auth/login")) {
            return true;
        }
        
        return false;
    }
}