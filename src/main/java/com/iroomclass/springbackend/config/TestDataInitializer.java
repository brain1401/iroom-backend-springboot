package com.iroomclass.springbackend.config;

import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.entity.UserRole;
import com.iroomclass.springbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테스트 환경에서 초기 데이터를 생성하는 컴포넌트
 * 
 * <p>H2 인메모리 데이터베이스 환경에서 JWT 인증 테스트를 위한 
 * 관리자 계정을 자동으로 생성합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
@Profile("test-local") // test-local 프로파일에서만 실행
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeTestAdminUser();
    }
    
    /**
     * 테스트용 관리자 사용자 생성
     * 
     * <p>JWT 인증 테스트를 위한 기본 관리자 계정을 생성합니다.</p>
     */
    private void initializeTestAdminUser() {
        String testUsername = "admin";
        
        // 이미 존재하는지 확인
        if (userRepository.findByUsername(testUsername).isPresent()) {
            log.info("테스트 관리자 계정이 이미 존재합니다: {}", testUsername);
            return;
        }
        
        // 테스트용 관리자 계정 생성
        User testAdmin = User.builder()
            .username(testUsername)
            .password(passwordEncoder.encode("password"))  // BCrypt 암호화
            .name("테스트 관리자")
            .email("admin@test.com")
            .academyName("테스트 학원")
            .role(UserRole.ADMIN)
            .build();
        
        // 데이터베이스 저장
        User savedAdmin = userRepository.save(testAdmin);
        
        log.info("테스트 관리자 계정 생성 완료: username={}, id={}", 
                savedAdmin.getUsername(), savedAdmin.getId());
        log.info("테스트 로그인 정보 - 사용자명: admin, 비밀번호: password");
    }
}