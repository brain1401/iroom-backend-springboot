package com.iroomclass.springbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 로그인 응답 DTO
 * 
 * <p>관리자 로그인 성공 시 반환되는 데이터를 전달하는 DTO입니다.
 * JWT 토큰과 관리자 기본 정보를 포함합니다.</p>
 * 
 * @param token JWT 액세스 토큰
 * @param username 사용자명
 * @param name 관리자 이름
 * @param role 사용자 역할
 * @param email 이메일 주소
 * @param academyName 학원명
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    name = "LoginResponse",
    description = "관리자 로그인 성공 응답 DTO",
    example = """
        {
          "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
          "username": "admin01",
          "name": "김관리",
          "role": "ADMIN",
          "email": "admin01@iroom.com",
          "academyName": "이룸클래스"
        }
        """
)
@Builder
public record LoginResponse(
        
    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "관리자 사용자명", example = "admin01")
    String username,
    
    @Schema(description = "관리자 이름", example = "김관리")
    String name,
    
    @Schema(description = "사용자 역할", example = "ADMIN")
    String role,
    
    @Schema(description = "이메일 주소", example = "admin01@iroom.com")
    String email,
    
    @Schema(description = "학원명", example = "이룸클래스")
    String academyName
    
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
}