package com.iroomclass.springbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 관리자 로그인 요청 DTO
 * 
 * <p>관리자 로그인 시 필요한 아이디와 비밀번호 정보를 담습니다.</p>
 */
@Schema(description = "관리자 로그인 요청")
public record AdminLoginRequest(
    @NotBlank(message = "아이디는 필수입니다")
    @Schema(description = "관리자 아이디", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "관리자 비밀번호", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
    public AdminLoginRequest {
        if (username != null) {
            username = username.trim();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}
