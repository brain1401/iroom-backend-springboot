package com.iroomclass.springbackend.domain.admin.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iroomclass.springbackend.domain.admin.entity.Admin;
import com.iroomclass.springbackend.domain.admin.repository.AdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 로그인 처리
     * @param username 사용자명
     * @param password 비밀번호
     * @return 로그인 성공한 관리자 정보
     * @throws RuntimeException 로그인 실패 시
     */
    public Admin login(String username, String password) {
        log.info("관리자 로그인 시도: {}", username);
        
        // 1단계: 사용자명으로 관리자 찾기
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));
        
        // 2단계: 비밀번호 검증
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            log.warn("잘못된 비밀번호로 로그인 시도: {}", username);
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        
        log.info("관리자 로그인 성공: {}", username);
        return admin;
    }

    /**
     * 학원명 조회
     * @return 학원명 (기본값: "이룸클래스")
     */
    public String getAcademyName() {
        try {
            // 1단계 : 데이터베이스에서 첫 번째 관리자 정보 가져오기
            // findFirstByOrderByIdAsc()는 ID가 가장 작은(첫 번째) 관리자를 가져옴
            Optional<Admin> adminOptional = adminRepository.findFirstByOrderByIdAsc();

            // 2단계 : 관리자가 존재하면 학원명 반환, 없으면 기본값 반환
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();
                String academyName = admin.getAcademyName();

                // 학원명이 비어있으면 기본값 사용
                if (academyName == null || academyName.trim().isEmpty()) {
                    log.info("학원명이 설정되지 않음, 기본값 사용: 이룸클래스");
                    return "이룸클래스";
                }

                log.info("학원명 조회 성공: {}", academyName);
                return academyName;
            } else {
                log.warn("관리자 정보가 존재하지 않음, 기본값 사용: 이룸클래스");
                return "이룸클래스";
            }
        } catch (Exception e) {
            log.error("학원명 조회 중 오류 발생: {}", e.getMessage());
            return "이룸클래스"; // 오류 발생 시에도 기본값 반환
        }
    }
}