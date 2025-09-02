package com.iroomclass.springbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 통합 로그인 응답 DTO
 * 
 * <p>학생과 관리자 로그인에 대한 통합 응답 DTO입니다.
 * 모든 사용자에게 JWT 토큰을 발급하여 일관된 인증 시스템을 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    name = "UnifiedLoginResponse",
    description = """
        통합 로그인 응답 DTO
        
        userType에 따라 포함되는 필드가 달라집니다:
        - STUDENT: token, refreshToken, userId, name, role(STUDENT), phone, grade, birthDate, userType
        - TEACHER: token, refreshToken, userId, username, name, role(ADMIN), email, academyName, userType
        """,
    example = """
        {
          "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
          "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "name": "김철수",
          "role": "STUDENT",
          "phone": "010-1234-5678",
          "grade": 1,
          "birthDate": "2008-03-15",
          "userType": "STUDENT"
        }
        """
)
@Builder
public record UnifiedLoginResponse(
        
    @Schema(description = "JWT 액세스 토큰 (24시간 유효)", 
            example = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "JWT 리프레시 토큰 (30일 유효)", 
            example = "eyJhbGciOiJIUzUxMiJ9...")
    String refreshToken,
    
    @Schema(description = "사용자 고유 ID", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,
    
    @Schema(description = "사용자명 (TEACHER 타입에서만 값 존재, STUDENT는 null)", 
            example = "admin01",
            nullable = true)
    String username,
    
    @Schema(description = "이름 (모든 타입에서 필수)", 
            example = "김철수")
    String name,
    
    @Schema(description = "사용자 역할 (DB 저장값)", 
            example = "STUDENT",
            allowableValues = {"STUDENT", "ADMIN"})
    String role,
    
    @Schema(description = "이메일 (TEACHER 타입에서만 값 존재, STUDENT는 null)", 
            example = "admin@example.com",
            nullable = true)
    String email,
    
    @Schema(description = "전화번호 (STUDENT 타입에서만 값 존재, TEACHER는 null)", 
            example = "010-1234-5678",
            nullable = true)
    String phone,
    
    @Schema(description = "학년 (STUDENT 타입에서만 값 존재, TEACHER는 null)", 
            example = "1",
            nullable = true)
    Integer grade,
    
    @Schema(description = "생년월일 (STUDENT 타입에서만 값 존재, TEACHER는 null)", 
            example = "2008-03-15",
            nullable = true)
    LocalDate birthDate,
    
    @Schema(description = "학원명 (TEACHER 타입에서만 값 존재, STUDENT는 null)", 
            example = "이룸클래스",
            nullable = true)
    String academyName,
    
    @Schema(description = "사용자 타입 (요청 시 전달한 userType)", 
            example = "STUDENT",
            allowableValues = {"STUDENT", "TEACHER"})
    String userType
    
) {
    
    /**
     * 로그인 성공 여부 확인
     * 
     * @return 토큰이 있으면 true, 없으면 false
     */
    public boolean isSuccess() {
        return token != null && !token.isBlank();
    }
    
    /**
     * 학생 응답 여부 확인
     * 
     * @return 학생이면 true
     */
    public boolean isStudent() {
        return "STUDENT".equals(role);
    }
    
    /**
     * 관리자 응답 여부 확인
     * 
     * @return 관리자면 true
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    /**
     * 마스킹된 토큰 반환 (보안상 로그용)
     * 
     * @return 마스킹된 JWT 토큰
     */
    public String getMaskedToken() {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
    
    /**
     * 기존 LoginResponse로 변환 (호환성)
     * 
     * @return LoginResponse 객체
     */
    public LoginResponse toLoginResponse() {
        if (!isAdmin()) {
            throw new IllegalStateException("관리자가 아닌 경우 LoginResponse로 변환할 수 없습니다");
        }
        
        return LoginResponse.builder()
                .token(token)
                .username(username)
                .name(name)
                .role(role)
                .email(email)
                .academyName(academyName)
                .build();
    }
    
    /**
     * 기존 StudentLoginResponse로 변환 (호환성)
     * 
     * @return StudentLoginResponse 객체
     */
    public StudentLoginResponse toStudentLoginResponse() {
        if (!isStudent()) {
            throw new IllegalStateException("학생이 아닌 경우 StudentLoginResponse로 변환할 수 없습니다");
        }
        
        return new StudentLoginResponse(
                userId, 
                name, 
                phone, 
                grade, 
                birthDate,
                "로그인에 성공했습니다"
        );
    }
    
    /**
     * User 엔티티에서 UnifiedLoginResponse 생성 (팩토리 메서드)
     * 
     * @param user 사용자 엔티티
     * @param token JWT 액세스 토큰
     * @param refreshToken JWT 리프레시 토큰
     * @return UnifiedLoginResponse 객체
     */
    public static UnifiedLoginResponse from(com.iroomclass.springbackend.domain.user.entity.User user, 
                                            String token, String refreshToken) {
        return UnifiedLoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .email(user.getEmail())
                .phone(user.getPhone())
                .grade(user.getGrade())
                .birthDate(user.getBirthDate())
                .academyName(user.getAcademyName())
                .userType(user.isStudent() ? "STUDENT" : "TEACHER")
                .build();
    }
}