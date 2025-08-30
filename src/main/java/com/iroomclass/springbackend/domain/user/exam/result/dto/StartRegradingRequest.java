package com.iroomclass.springbackend.domain.user.exam.result.dto;

import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 재채점 시작 요청 DTO
 * 
 * @param originalResultId 기존 채점 결과 ID
 * @param newGraderId 새로운 채점자 ID
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record StartRegradingRequest(
    @NotNull(message = "기존 채점 결과 ID는 필수입니다")
    @Schema(description = "기존 채점 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID originalResultId,
    
    @NotNull(message = "새로운 채점자 ID는 필수입니다")
    @Schema(description = "새로운 채점자 ID", example = "123e4567-e89b-12d3-a456-426614174002", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID newGraderId
) {
    
    /**
     * Compact Constructor로 유효성 검증
     */
    public StartRegradingRequest {
        Objects.requireNonNull(originalResultId, "기존 채점 결과 ID는 필수입니다");
        Objects.requireNonNull(newGraderId, "새로운 채점자 ID는 필수입니다");
    }
}