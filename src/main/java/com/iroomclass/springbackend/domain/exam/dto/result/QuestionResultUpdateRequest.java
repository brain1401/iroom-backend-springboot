package com.iroomclass.springbackend.domain.exam.dto.result;

import java.math.BigDecimal;
import java.util.Objects;

import com.iroomclass.springbackend.common.BaseRecord;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion.ScoringMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 문제별 채점 결과 수정 요청 DTO
 * 
 * @param isCorrect       정답 여부
 * @param score           획득 점수
 * @param scoringMethod   채점 방법
 * @param confidenceScore 신뢰도 점수
 * @param feedback        피드백
 * @param aiAnalysis      AI 분석 결과
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record QuestionResultUpdateRequest(
        @Schema(description = "정답 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "정답 여부는 필수입니다") Boolean isCorrect,

        @Schema(description = "획득 점수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "획득 점수는 필수입니다") @Min(value = 0, message = "점수는 0 이상이어야 합니다") @Max(value = 100, message = "점수는 100 이하여야 합니다") Integer score,

        @Schema(description = "채점 방법", example = "MANUAL") ScoringMethod scoringMethod,

        @Schema(description = "신뢰도 점수", example = "0.95") @DecimalMin(value = "0.0", message = "신뢰도 점수는 0.0 이상이어야 합니다") @DecimalMax(value = "1.0", message = "신뢰도 점수는 1.0 이하여야 합니다") BigDecimal confidenceScore,

        @Schema(description = "피드백", example = "부분 점수를 부여합니다.") String feedback,

        @Schema(description = "AI 분석 결과", example = "답변이 부분적으로 정확합니다.") String aiAnalysis) implements BaseRecord {

    /**
     * Compact constructor with validation
     */
    public QuestionResultUpdateRequest {
        Objects.requireNonNull(isCorrect, "정답 여부는 필수입니다");
        Objects.requireNonNull(score, "획득 점수는 필수입니다");

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("점수는 0 이상 100 이하여야 합니다");
        }
        if (confidenceScore != null
                && (confidenceScore.compareTo(BigDecimal.ZERO) < 0 || confidenceScore.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("신뢰도 점수는 0.0 이상 1.0 이하여야 합니다");
        }
    }
}