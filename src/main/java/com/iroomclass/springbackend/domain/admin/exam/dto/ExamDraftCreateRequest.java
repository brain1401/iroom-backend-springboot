package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 초안 생성 요청 DTO
 * 
 * 시험지 초안 생성 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 초안 생성 요청")
public record ExamDraftCreateRequest(
    @NotBlank(message = "시험지 이름은 필수입니다.")
    @Schema(description = "시험지 이름", example = "1학년 중간고사", requiredMode = Schema.RequiredMode.REQUIRED)
    String examName,
    
    @NotNull(message = "학년은 필수입니다.")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 3, message = "학년은 3 이하여야 합니다.")
    @Schema(description = "학년 (1, 2, 3)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer grade,
    
    @NotNull(message = "총 문제 개수는 필수입니다.")
    @Min(value = 1, message = "총 문제 개수는 1 이상이어야 합니다.")
    @Max(value = 30, message = "총 문제 개수는 30 이하여야 합니다.")
    @Schema(description = "총 문제 개수 (1~30)", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer totalQuestions,
    
    @NotEmpty(message = "선택된 단원은 최소 1개 이상이어야 합니다.")
    @Schema(description = "선택된 단원 ID 목록", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> unitIds
) {
    public ExamDraftCreateRequest {
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(totalQuestions, "totalQuestions는 필수입니다");
        Objects.requireNonNull(unitIds, "unitIds는 필수입니다");
    }
}