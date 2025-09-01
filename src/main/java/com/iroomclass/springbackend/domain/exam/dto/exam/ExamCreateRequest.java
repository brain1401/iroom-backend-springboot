package com.iroomclass.springbackend.domain.exam.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import com.iroomclass.springbackend.common.ValidationConstants;

/**
 * 시험 등록 요청 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 등록 요청")
public record ExamCreateRequest(
        @NotNull(message = ValidationConstants.REQUIRED_EXAM_SHEET_ID) @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED) UUID examSheetId,

        @Schema(description = "시험 관련 메모/설명", example = "1학년 중간고사 - 자연수와 0 단원") String content,

        @NotNull(message = ValidationConstants.REQUIRED_STUDENT_COUNT) @Min(value = 1, message = ValidationConstants.INVALID_STUDENT_COUNT) @Schema(description = "학생 수", example = "25", requiredMode = Schema.RequiredMode.REQUIRED) Integer studentCount) {
    public ExamCreateRequest {
        Objects.requireNonNull(examSheetId, ValidationConstants.getNullCheckMessage("examSheetId"));
        Objects.requireNonNull(studentCount, ValidationConstants.getNullCheckMessage("studentCount"));
    }
}