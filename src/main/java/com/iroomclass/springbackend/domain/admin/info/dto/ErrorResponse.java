package com.iroomclass.springbackend.domain.admin.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * 에러 응답 DTO
 * 
 * API 호출 시 발생하는 에러 정보를 담습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "에러 응답 정보")
public record ErrorResponse(
    @Schema(description = "에러 메시지", example = "존재하지 않는 관리자 아이디")
    String message
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public ErrorResponse {
        Objects.requireNonNull(message, "message는 필수입니다");
    }
}