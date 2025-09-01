package com.iroomclass.springbackend.domain.user.service;

import com.iroomclass.springbackend.config.JwtUtil;
import com.iroomclass.springbackend.domain.user.dto.LoginRequest;
import com.iroomclass.springbackend.domain.user.dto.LoginResponse;
import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 * 
 * <p>로그인, 로그아웃, JWT 토큰 발급 등 인증 관련 비즈니스 로직을 처리합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 관리자 로그인
     * 
     * @param loginRequest 로그인 요청
     * @return 로그인 응답 (JWT 토큰 포함)
     * @throws BadCredentialsException 인증 실패 시
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("관리자 로그인 시도: {}", loginRequest.username());
        
        // 사용자명으로 사용자 찾기
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자명: {}", loginRequest.username());
                    return new BadCredentialsException("잘못된 사용자명 또는 비밀번호입니다");
                });
        
        // 로그인 가능 여부 확인 (관리자이고 username, password가 있는지)
        if (!user.canLogin()) {
            log.warn("로그인 방지된 사용자: {} (역할: {})", loginRequest.username(), user.getRole());
            throw new BadCredentialsException("로그인 권한이 없는 사용자입니다");
        }
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            log.warn("잘못된 비밀번호: {}", loginRequest.username());
            throw new BadCredentialsException("잘못된 사용자명 또는 비밀번호입니다");
        }
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(
            user.getUsername(), 
            user.getId(), 
            user.getRole().name()
        );
        
        log.info("관리자 로그인 성공: {} (역할: {})", user.getUsername(), user.getRole());
        
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .email(user.getEmail())
                .academyName(user.getAcademyName())
                .build();
    }
    
    /**
     * 사용자명 중복 확인
     * 
     * @param username 확인할 사용자명
     * @return 중복이면 true, 아니면 false
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}