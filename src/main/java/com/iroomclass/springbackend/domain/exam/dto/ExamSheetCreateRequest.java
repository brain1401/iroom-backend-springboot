package com.iroomclass.springbackend.domain.exam.dto;

import java.util.List;
import java.util.UUID;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 생성 요청 DTO
 * 
 * 시험지 생성 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 생성 요청")
public record ExamSheetCreateRequest(
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
    
    @NotNull(message = "객관식 문제 개수는 필수입니다.")
    @Min(value = 0, message = "객관식 문제 개수는 0 이상이어야 합니다.")
    @Max(value = 30, message = "객관식 문제 개수는 30 이하여야 합니다.")
    @Schema(description = "객관식 문제 개수 (0~30)", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer multipleChoiceCount,
    
    @NotNull(message = "주관식 문제 개수는 필수입니다.")
    @Min(value = 0, message = "주관식 문제 개수는 0 이상이어야 합니다.")
    @Max(value = 30, message = "주관식 문제 개수는 30 이하여야 합니다.")
    @Schema(description = "주관식 문제 개수 (0~30)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer subjectiveCount,
    
    @NotEmpty(message = "선택된 단원은 최소 1개 이상이어야 합니다.")
    @Schema(description = "선택된 단원 ID 목록", example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"123e4567-e89b-12d3-a456-426614174001\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    List<UUID> unitIds
) {
    public ExamSheetCreateRequest {
        // 이중 검증 아키텍처: 핵심 불변성은 Compact Constructor에서 즉시 검증
        
        // 문제 개수 일관성 검증 - 이는 데이터 무결성의 핵심 불변성
        if (totalQuestions != null && multipleChoiceCount != null && subjectiveCount != null) {
            int calculatedTotal = multipleChoiceCount + subjectiveCount;
            if (calculatedTotal != totalQuestions) {
                throw new IllegalArgumentException(
                    String.format("객관식 문제 개수(%d) + 주관식 문제 개수(%d) = %d가 총 문제 개수(%d)와 일치하지 않습니다.",
                        multipleChoiceCount, subjectiveCount, calculatedTotal, totalQuestions)
                );
            }
        }
    }
}