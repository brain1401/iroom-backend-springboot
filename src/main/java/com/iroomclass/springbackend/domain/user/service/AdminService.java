package com.iroomclass.springbackend.domain.user.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iroomclass.springbackend.domain.user.entity.Admin;
import com.iroomclass.springbackend.domain.user.repository.AdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 인증 정보 검증
     * @param username 사용자명
     * @param password 비밀번호
     * @return 인증 성공한 관리자 정보
     * @throws RuntimeException 인증 실패 시
     */
    public Admin verifyCredentials(String username, String password) {
        log.info("관리자 인증 정보 검증 시도: {}", username);
        
        // 1단계: 사용자명으로 관리자 찾기
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));
        
        // 2단계: 비밀번호 검증
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            log.warn("잘못된 비밀번호로 인증 시도: {}", username);
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        
        log.info("관리자 인증 정보 검증 성공: {}", username);
        return admin;
    }
}