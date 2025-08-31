package com.iroomclass.springbackend.domain.exam.dto.result;

import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 채점 완료 요청 DTO
 * 
 * @param resultId 시험 결과 ID
 * @param comment  채점 코멘트
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record CompleteGradingRequest(
        @NotNull(message = "시험 결과 ID는 필수입니다") @Schema(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) UUID resultId,

        @Size(max = 1000, message = "채점 코멘트는 1000자 이하여야 합니다") @Schema(description = "채점 코멘트", example = "전체적으로 잘 답변했습니다. 문제 3번의 설명이 조금 부족하지만 이해도는 충분합니다.") String comment) {

    /**
     * Compact Constructor로 유효성 검증
     */
    public CompleteGradingRequest {
        Objects.requireNonNull(resultId, "시험 결과 ID는 필수입니다");

        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("채점 코멘트는 1000자 이하여야 합니다");
        }
    }
}