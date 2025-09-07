package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 시험 생성 요청 DTO
 * 
 * <p>
 * 프론트엔드에서 시험 생성 시 필요한 최소한의 정보만 포함합니다.
 * 나머지 정보는 백엔드에서 ExamSheet 정보를 통해 자동으로 채워집니다.
 * </p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 생성 요청 DTO")
public record CreateExamRequest(
        @Schema(description = "시험명", example = "2024년 2학기 중간고사", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "시험명은 필수입니다") @Size(max = 100, message = "시험명은 100자 이하여야 합니다") String examName,

        @Schema(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "시험지 ID는 필수입니다") UUID examSheetId,

        @Schema(description = "최대 학생 수", example = "30", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "최대 학생 수는 필수입니다") @Min(value = 1, message = "최대 학생 수는 1명 이상이어야 합니다") Integer maxStudent,

        @Schema(description = "시험 설명", example = "2학기 중간고사입니다", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Size(max = 500, message = "시험 설명은 500자 이하여야 합니다") String description,

        @Schema(description = "대상 학년", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "대상 학년은 필수입니다") @Min(value = 1, message = "대상 학년은 1 이상이어야 합니다") Integer grade,

        @Schema(description = "시험 시작일시 (선택)", example = "2024-12-10T09:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED) LocalDateTime startDate,

        @Schema(description = "시험 종료일시 (선택)", example = "2024-12-10T11:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED) LocalDateTime endDate,

        @Schema(description = "시험 제한시간 (분 단위, 선택)", example = "120", requiredMode = Schema.RequiredMode.NOT_REQUIRED) Integer duration) {
    /**
     * Compact constructor로 추가 검증
     */
    public CreateExamRequest {
        // 시작일과 종료일이 모두 있을 경우 검증
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("시험 종료일시는 시작일시보다 늦어야 합니다");
        }

        // 제한시간 검증
        if (duration != null && duration <= 0) {
            throw new IllegalArgumentException("시험 제한시간은 0보다 커야 합니다");
        }
    }
}