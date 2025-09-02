package com.iroomclass.springbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 리프레시 토큰 요청 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "리프레시 토큰 요청")
public record RefreshTokenRequest(
    
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    String refreshToken
    
) {
    public RefreshTokenRequest {
        if (refreshToken != null && refreshToken.isBlank()) {
            throw new IllegalArgumentException("리프레시 토큰은 공백일 수 없습니다");
        }
    }
}