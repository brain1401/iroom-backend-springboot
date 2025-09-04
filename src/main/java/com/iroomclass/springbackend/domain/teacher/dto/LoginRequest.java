package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 선생님 로그인 요청 DTO
 */
@Schema(description = "선생님 로그인 요청")
public record LoginRequest(
    @NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "사용자명", example = "teacher01", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
    public LoginRequest {
        if (username != null) {
            username = username.trim();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}