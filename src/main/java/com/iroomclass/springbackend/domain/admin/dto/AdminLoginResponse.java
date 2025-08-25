package com.iroomclass.springbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 관리자 로그인 응답 DTO
 * 
 * <p>로그인 성공 시 반환되는 정보를 담습니다.</p>
 */
@Schema(description = "관리자 로그인 응답")
public record AdminLoginResponse(
    @Schema(description = "로그인한 관리자 아이디", example = "admin")
    String username,

    @Schema(description = "로그인 결과 메시지", example = "로그인에 성공했습니다")
    String message,

    @Schema(description = "로그인 시간", example = "2025-01-01T00:00:00")
    LocalDateTime loginTime
) {}
