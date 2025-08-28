package com.iroomclass.springbackend.domain.admin.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * 로그인 요청 DTO
 * 
 * 관리자 로그인 시 필요한 정보를 담습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "로그인 요청 정보")
public record LoginRequest(
    @Schema(description = "관리자 아이디", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(description = "관리자 비밀번호", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public LoginRequest {
        Objects.requireNonNull(username, "username은 필수입니다");
        Objects.requireNonNull(password, "password는 필수입니다");
    }
}
