package com.iroomclass.springbackend.domain.exam.dto.result;

import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import com.iroomclass.springbackend.common.ValidationConstants;

/**
 * 채점 시작 요청 DTO
 * 
 * @param submissionId  시험 제출 ID
 * @param graderId      채점자 ID (자동 채점시 null)
 * @param isAutoGrading 자동 채점 여부
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record StartGradingRequest(
        @NotNull(message = ValidationConstants.REQUIRED_SUBMISSION_ID) @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001", requiredMode = Schema.RequiredMode.REQUIRED) UUID submissionId,

        @Schema(description = "채점자 ID (자동 채점시 null)", example = "123e4567-e89b-12d3-a456-426614174002") UUID graderId,

        @Schema(description = "자동 채점 여부", example = "false", defaultValue = "false") Boolean isAutoGrading) {

    /**
     * Compact Constructor로 유효성 검증
     */
    public StartGradingRequest {
        Objects.requireNonNull(submissionId, ValidationConstants.REQUIRED_SUBMISSION_ID);

        if (isAutoGrading == null) {
            isAutoGrading = false;
        }

        // 자동 채점이 아닌 경우 채점자 ID는 필수
        if (!isAutoGrading && graderId == null) {
            throw new IllegalArgumentException(ValidationConstants.REQUIRED_GRADER_ID_FOR_MANUAL);
        }

        // 자동 채점인 경우 채점자 ID는 null이어야 함
        if (isAutoGrading && graderId != null) {
            throw new IllegalArgumentException(ValidationConstants.GRADER_ID_NULL_FOR_AUTO);
        }
    }
}