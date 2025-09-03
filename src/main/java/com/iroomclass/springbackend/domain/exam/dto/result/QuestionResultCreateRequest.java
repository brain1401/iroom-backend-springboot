package com.iroomclass.springbackend.domain.exam.dto.result;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.iroomclass.springbackend.common.BaseRecord;
import com.iroomclass.springbackend.domain.exam.entity.ExamResultQuestion.ScoringMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 문제별 채점 결과 생성 요청 DTO
 * 
 * @param questionId      문제 ID
 * @param answerSheetId   답안지 ID
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
public record QuestionResultCreateRequest(
        @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174005", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "문제 ID는 필수입니다") UUID questionId,

        @Schema(description = "답안지 ID", example = "123e4567-e89b-12d3-a456-426614174004", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "답안지 ID는 필수입니다") UUID answerSheetId,

        @Schema(description = "정답 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "정답 여부는 필수입니다") Boolean isCorrect,

        @Schema(description = "획득 점수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "획득 점수는 필수입니다") @Min(value = 0, message = "점수는 0 이상이어야 합니다") @Max(value = 100, message = "점수는 100 이하여야 합니다") Integer score,

        @Schema(description = "채점 방법", example = "AUTO", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "채점 방법은 필수입니다") ScoringMethod scoringMethod,

        @Schema(description = "신뢰도 점수", example = "0.95") @DecimalMin(value = "0.0", message = "신뢰도 점수는 0.0 이상이어야 합니다") @DecimalMax(value = "1.0", message = "신뢰도 점수는 1.0 이하여야 합니다") BigDecimal confidenceScore,

        @Schema(description = "피드백", example = "정답입니다.") String feedback,

        @Schema(description = "AI 분석 결과", example = "답변이 정확하고 논리적입니다.") String aiAnalysis) implements BaseRecord {

    /**
     * Compact constructor with validation
     */
    public QuestionResultCreateRequest {
        Objects.requireNonNull(questionId, "문제 ID는 필수입니다");
        Objects.requireNonNull(answerSheetId, "답안지 ID는 필수입니다");
        Objects.requireNonNull(isCorrect, "정답 여부는 필수입니다");
        Objects.requireNonNull(score, "획득 점수는 필수입니다");
        Objects.requireNonNull(scoringMethod, "채점 방법은 필수입니다");

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("점수는 0 이상 100 이하여야 합니다");
        }
        if (confidenceScore != null
                && (confidenceScore.compareTo(BigDecimal.ZERO) < 0 || confidenceScore.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("신뢰도 점수는 0.0 이상 1.0 이하여야 합니다");
        }
    }
}