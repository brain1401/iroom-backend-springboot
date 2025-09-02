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

import java.util.UUID;

/**
 * 통합 인증 서비스
 * 
 * *
 * <p>
 * 학생과 관리자의 모든 로그인 요청을 처리하는 통합 인증 서비스입니다.
 * userType에 따라 다른 인증 방식을 적용하며, 모든 사용자에게 JWT 토큰을 발급합니다.
 * </p>
 * 
 * <ul>
 * <li><strong>STUDENT</strong>: 3-factor 인증 (이름 + 전화번호 + 생년월일)</li>
 * <li><strong>TEACHER</strong>: 기본 인증 (사용자명 + 비밀번호)</li>
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
     * <p>
     * 도메인에 따라 적절한 인증 방식을 선택하여 로그인을 처리합니다.
     * </p>
     * 
     * @param loginRequest 통합 로그인 요청
     * @return 통합 로그인 응답 (JWT 토큰 포함)
     * @throws BadCredentialsException 인증 실패 시
     */
    @Transactional // 클래스 레벨의 readOnly=true를 오버라이드하여 쓰기 가능하도록 설정
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

            // JWT 토큰과 Refresh Token 생성
            String token = jwtUtil.generateToken(
                    user.getUsername() != null ? user.getUsername() : user.getName(),
                    user.getId(),
                    user.getRole().name());

            String refreshToken = jwtUtil.generateRefreshToken(
                    user.getUsername() != null ? user.getUsername() : user.getName(),
                    user.getId(),
                    user.getRole().name());

            // Refresh Token을 사용자 정보에 저장
            updateUserRefreshToken(user, refreshToken);

            // 통합 응답 생성
            UnifiedLoginResponse response = UnifiedLoginResponse.from(user, token, refreshToken);

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
     * <p>
     * 이름, 전화번호, 생년월일을 사용하여 학생을 인증합니다.
     * </p>
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
                        UserRole.STUDENT)
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
     * <p>
     * 사용자명과 비밀번호를 사용하여 관리자를 인증합니다.
     * </p>
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

    /**
     * 사용자의 Refresh Token 업데이트
     * 
     * @param user         사용자 엔티티
     * @param refreshToken 새로운 refresh token
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private void updateUserRefreshToken(User user, String refreshToken) {
        // 새로운 트랜잭션에서 refresh token 업데이트 실행
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + user.getId()));

        existingUser.setRefreshToken(refreshToken);
        userRepository.saveAndFlush(existingUser); // 즉시 DB에 반영

        log.info("Refresh Token 저장 완료: userId={}", user.getId());
    }

    /**
     * Refresh Token으로 Access Token 갱신
     * 
     * @param refreshToken 유효한 refresh token
     * @return 새로운 UnifiedLoginResponse (새 access token 포함)
     * @throws BadCredentialsException refresh token이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public UnifiedLoginResponse refreshToken(String refreshToken) {
        log.info("Refresh Token을 통한 Access Token 갱신 요청");

        try {
            // Refresh Token 유효성 검증
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                log.warn("유효하지 않은 Refresh Token");
                throw new BadCredentialsException("유효하지 않은 Refresh Token입니다");
            }

            // DB에서 해당 refresh token을 가진 사용자 찾기
            User user = userRepository.findByRefreshToken(refreshToken)
                    .orElseThrow(() -> {
                        log.warn("DB에서 Refresh Token을 찾을 수 없음");
                        return new BadCredentialsException("등록되지 않은 Refresh Token입니다");
                    });

            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);

            // 새로운 Refresh Token 생성 및 저장 (보안 강화)
            String newRefreshToken = jwtUtil.generateRefreshToken(
                    user.getUsername() != null ? user.getUsername() : user.getName(),
                    user.getId(),
                    user.getRole().name());

            updateUserRefreshToken(user, newRefreshToken);

            // 응답 생성
            UnifiedLoginResponse response = UnifiedLoginResponse.from(user, newAccessToken, newRefreshToken);

            log.info("Access Token 갱신 성공: userId={}, 사용자={}", user.getId(), user.getName());

            return response;

        } catch (BadCredentialsException e) {
            log.warn("Refresh Token 갱신 실패: 사유={}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Refresh Token 갱신 오류: 오류={}", e.getMessage(), e);
            throw new BadCredentialsException("토큰 갱신 중 오류가 발생했습니다");
        }
    }

    /**
     * 로그아웃 시 Refresh Token 무효화
     * 
     * @param userId 사용자 ID
     */
    @Transactional
    public void invalidateRefreshToken(UUID userId) {
        log.info("Refresh Token 무효화: userId={}", userId);

        userRepository.findById(userId).ifPresent(user -> {
            updateUserRefreshToken(user, null); // refresh token을 null로 설정
        });
    }

    /**
     * Refresh Token으로 사용자 조회
     * 
     * @param refreshToken 리프레시 토큰
     * @return 사용자 엔티티 (없으면 null)
     * @throws BadCredentialsException refresh token이 유효하지 않은 경우
     */
    @Transactional(readOnly = true)
    public User getUserByRefreshToken(String refreshToken) {
        try {
            // 1. JWT 토큰 형식 검증
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다");
            }

            // 2. DB에서 해당 refresh token을 가진 사용자 찾기
            return userRepository.findByRefreshToken(refreshToken)
                    .orElseThrow(() -> new BadCredentialsException("등록되지 않은 리프레시 토큰입니다"));

        } catch (BadCredentialsException e) {
            log.warn("리프레시 토큰으로 사용자 조회 실패: 사유={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰으로 사용자 조회 오류: 오류={}", e.getMessage(), e);
            throw new BadCredentialsException("사용자 조회 중 오류가 발생했습니다");
        }
    }
}