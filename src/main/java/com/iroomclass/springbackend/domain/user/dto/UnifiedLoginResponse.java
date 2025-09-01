package com.iroomclass.springbackend.domain.user.dto;

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
    description = "통합 로그인 응답 DTO",
    example = """
        {
          "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "username": "admin01",
          "name": "김철수",
          "role": "STUDENT",
          "email": "admin@example.com",
          "phone": "010-1234-5678",
          "grade": 1,
          "birthDate": "2008-03-15",
          "academyName": "이룸클래스",
          "userType": "STUDENT"
        }
        """
)
@Builder
public record UnifiedLoginResponse(
        
    @Schema(description = "JWT 액세스 토큰", 
            example = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "사용자 고유 ID", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,
    
    @Schema(description = "사용자명 (관리자만)", 
            example = "admin01")
    String username,
    
    @Schema(description = "이름", 
            example = "김철수")
    String name,
    
    @Schema(description = "사용자 역할", 
            example = "STUDENT",
            allowableValues = {"STUDENT", "ADMIN"})
    String role,
    
    @Schema(description = "이메일 (관리자만)", 
            example = "admin@example.com")
    String email,
    
    @Schema(description = "전화번호 (학생만)", 
            example = "010-1234-5678")
    String phone,
    
    @Schema(description = "학년 (학생만)", 
            example = "1")
    Integer grade,
    
    @Schema(description = "생년월일 (학생만)", 
            example = "2008-03-15")
    LocalDate birthDate,
    
    @Schema(description = "학원명 (관리자만)", 
            example = "이룸클래스")
    String academyName,
    
    @Schema(description = "사용자 타입", 
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
     * @param token JWT 토큰
     * @return UnifiedLoginResponse 객체
     */
    public static UnifiedLoginResponse from(com.iroomclass.springbackend.domain.user.entity.User user, 
                                            String token) {
        return UnifiedLoginResponse.builder()
                .token(token)
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