package com.iroomclass.springbackend.domain.exam.dto.result;

import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 수동 채점 처리 요청 DTO
 * 
 * @param resultId  문제별 결과 ID
 * @param score     채점 점수
 * @param isCorrect 정답 여부
 * @param feedback  피드백
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record ManualGradingRequest(
        @NotNull(message = "문제별 결과 ID는 필수입니다") @Schema(description = "문제별 결과 ID", example = "123e4567-e89b-12d3-a456-426614174003", requiredMode = Schema.RequiredMode.REQUIRED) UUID resultId,

        @NotNull(message = "채점 점수는 필수입니다") @Min(value = 0, message = "채점 점수는 0 이상이어야 합니다") @Max(value = 100, message = "채점 점수는 100 이하여야 합니다") @Schema(description = "채점 점수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED) Integer score,

        @NotNull(message = "정답 여부는 필수입니다") @Schema(description = "정답 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) Boolean isCorrect,

        @Size(max = 500, message = "피드백은 500자 이하여야 합니다") @Schema(description = "피드백", example = "정답입니다. 설명이 명확합니다.") String feedback) {

    /**
     * Compact Constructor로 유효성 검증
     */
    public ManualGradingRequest {
        Objects.requireNonNull(resultId, "문제별 결과 ID는 필수입니다");
        Objects.requireNonNull(score, "채점 점수는 필수입니다");
        Objects.requireNonNull(isCorrect, "정답 여부는 필수입니다");

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("채점 점수는 0~100 사이여야 합니다");
        }

        if (feedback != null && feedback.length() > 500) {
            throw new IllegalArgumentException("피드백은 500자 이하여야 합니다");
        }
    }
}