package com.iroomclass.springbackend.domain.admin.service;

import com.iroomclass.springbackend.domain.admin.dto.AdminLoginRequest;
import com.iroomclass.springbackend.domain.admin.dto.AdminLoginResponse;
import com.iroomclass.springbackend.domain.admin.entity.Admin;
import com.iroomclass.springbackend.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 관리자 서비스
 * 
 * <p>관리자 로그인 및 관련 비즈니스 로직을 처리합니다.
 * 비밀번호 검증과 로그인 상태 관리를 담당합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 관리자 로그인 처리
     * 
     * @param request 로그인 요청 정보
     * @return 로그인 응답 정보
     * @throws IllegalArgumentException 잘못된 로그인 정보일 때
     */
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        log.info("관리자 로그인 시도: {}", request.username());
        
        // 아이디로 관리자 조회
        Admin admin = adminRepository.findByUsername(request.username())
            .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다"));
        
        // 비밀번호 확인
        if (!checkPassword(request.password(), admin.getPassword())) {
            log.warn("잘못된 비밀번호로 로그인 시도: {}", request.username());
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다");
        }
        
        log.info("관리자 로그인 성공: {}", request.username());
        
        return new AdminLoginResponse(
            admin.getUsername(),
            "로그인에 성공했습니다",
            LocalDateTime.now()
        );
    }
    
    /**
     * 비밀번호 확인
     * 
     * @param rawPassword 입력받은 비밀번호
     * @param encodedPassword 저장된 암호화된 비밀번호
     * @return 비밀번호 일치 여부
     */
    private boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}