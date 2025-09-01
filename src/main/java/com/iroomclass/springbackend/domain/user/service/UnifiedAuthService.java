package com.iroomclass.springbackend.domain.user.service;

import com.iroomclass.springbackend.config.JwtUtil;
import com.iroomclass.springbackend.domain.user.dto.UnifiedLoginRequest;
import com.iroomclass.springbackend.domain.user.dto.UnifiedLoginResponse;
import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.entity.UserRole;
import com.iroomclass.springbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 인증 서비스
 * 
 * * <p>학생과 관리자의 모든 로그인 요청을 처리하는 통합 인증 서비스입니다.
 * userType에 따라 다른 인증 방식을 적용하며, 모든 사용자에게 JWT 토큰을 발급합니다.</p>
 * 
 * <ul>
 *   <li><strong>STUDENT</strong>: 3-factor 인증 (이름 + 전화번호 + 생년월일)</li>
 *   <li><strong>TEACHER</strong>: 기본 인증 (사용자명 + 비밀번호)</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnifiedAuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 통합 로그인 처리
     * 
     * <p>도메인에 따라 적절한 인증 방식을 선택하여 로그인을 처리합니다.</p>
     * 
     * @param loginRequest 통합 로그인 요청
     * @return 통합 로그인 응답 (JWT 토큰 포함)
     * @throws BadCredentialsException 인증 실패 시
     */
    public UnifiedLoginResponse login(UnifiedLoginRequest loginRequest) {
        log.info("통합 로그인 요청: 사용자타입={}", loginRequest.userType());
        
        try {
            User user;
            
            if (loginRequest.isStudent()) {
                user = authenticateStudent(loginRequest);
            } else if (loginRequest.isTeacher()) {
                user = authenticateTeacher(loginRequest);
            } else {
                throw new BadCredentialsException("지원되지 않는 사용자 타입입니다: " + loginRequest.userType());
            }
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(
                user.getUsername() != null ? user.getUsername() : user.getName(),
                user.getId(),
                user.getRole().name()
            );
            
            // 통합 응답 생성
            UnifiedLoginResponse response = UnifiedLoginResponse.from(user, token);
            
            log.info("통합 로그인 성공: 사용자타입={}, 사용자={}, 역할={}", 
                    loginRequest.userType(), user.getName(), user.getRole());
            
            return response;
            
        } catch (BadCredentialsException e) {
            log.warn("통합 로그인 실패: 사용자타입={}, 사유={}", loginRequest.userType(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("통합 로그인 오류: 사용자타입={}, 오류={}", loginRequest.userType(), e.getMessage(), e);
            throw new BadCredentialsException("로그인 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 학생 3-factor 인증
     * 
     * <p>이름, 전화번호, 생년월일을 사용하여 학생을 인증합니다.</p>
     * 
     * @param loginRequest 로그인 요청
     * @return 인증된 학생 User 엔티티
     * @throws BadCredentialsException 인증 실패 시
     */
    private User authenticateStudent(UnifiedLoginRequest loginRequest) {
        log.info("학생 3-factor 인증 시도: 이름={}, 전화번호={}, 생년월일={}", 
                loginRequest.name(), loginRequest.phone(), loginRequest.birthDate());
        
        // 3-factor 인증: 이름 + 전화번호 + 생년월일로 학생 찾기
        User student = userRepository
                .findByNameAndPhoneAndBirthDateAndRole(
                    loginRequest.name(),
                    loginRequest.phone(), 
                    loginRequest.birthDate(),
                    UserRole.STUDENT
                )
                .orElseThrow(() -> {
                    log.warn("학생 3-factor 인증 실패: 이름={}, 전화번호={}, 생년월일={}", 
                            loginRequest.name(), loginRequest.phone(), loginRequest.birthDate());
                    return new BadCredentialsException("이름, 전화번호, 생년월일이 일치하는 학생을 찾을 수 없습니다");
                });
        
        // 학생 역할 확인
        if (!student.isStudent()) {
            log.warn("학생이 아닌 사용자의 학생 로그인 시도: 이름={}, 역할={}", 
                    student.getName(), student.getRole());
            throw new BadCredentialsException("학생 계정이 아닙니다");
        }
        
        log.info("학생 3-factor 인증 성공: ID={}, 이름={}, 학년={}", 
                student.getId(), student.getName(), student.getGrade());
        
        return student;
    }
    
    /**
     * 관리자 기본 인증
     * 
     * <p>사용자명과 비밀번호를 사용하여 관리자를 인증합니다.</p>
     * 
     * @param loginRequest 로그인 요청
     * @return 인증된 관리자 User 엔티티
     * @throws BadCredentialsException 인증 실패 시
     */
    private User authenticateTeacher(UnifiedLoginRequest loginRequest) {
        log.info("관리자 기본 인증 시도: 사용자명={}", loginRequest.username());
        
        // 사용자명으로 관리자 찾기
        User admin = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 관리자 사용자명: {}", loginRequest.username());
                    return new BadCredentialsException("잘못된 사용자명 또는 비밀번호입니다");
                });
        
        // 로그인 가능 여부 확인 (관리자이고 username, password가 있는지)
        if (!admin.canLogin()) {
            log.warn("로그인 방지된 사용자: {} (역할: {})", loginRequest.username(), admin.getRole());
            throw new BadCredentialsException("로그인 권한이 없는 사용자입니다");
        }
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.password(), admin.getPassword())) {
            log.warn("잘못된 비밀번호: {}", loginRequest.username());
            throw new BadCredentialsException("잘못된 사용자명 또는 비밀번호입니다");
        }
        
        log.info("관리자 기본 인증 성공: 사용자명={}, 역할={}", 
                admin.getUsername(), admin.getRole());
        
        return admin;
    }
    
    /**
     * 사용자명 중복 확인
     * 
     * @param username 확인할 사용자명
     * @return 중복이면 true, 아니면 false
     */
    public boolean isUsernameExists(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.debug("사용자명 중복 확인: {}={}", username, exists);
        return exists;
    }
    
    /**
     * 도메인별 사용자 수 통계 조회 (관리 목적)
     * 
     * @param role 사용자 역할
     * @return 사용자 수
     */
    public long getUserCountByRole(UserRole role) {
        long count = userRepository.countByRole(role);
        log.debug("사용자 수 통계: {}={}", role, count);
        return count;
    }
}