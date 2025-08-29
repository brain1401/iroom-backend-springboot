package com.iroomclass.springbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * 비밀번호 암호화 테스트 클래스
 * admin123을 BCrypt로 암호화하여 MySQL에 저장할 값을 생성합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderTest {
    
    @Test
    void generateEncodedPassword() {
        // BCrypt 암호화 도구 생성
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // admin123을 암호화
        String encodedPassword = encoder.encode("admin123");
        
        // 결과 출력
        System.out.println("=== 암호화된 비밀번호 ===");
        System.out.println(encodedPassword);
        System.out.println("=====================");
        
        // 검증 테스트
        boolean matches = encoder.matches("admin123", encodedPassword);
        System.out.println("검증 결과: " + matches);
    }
}