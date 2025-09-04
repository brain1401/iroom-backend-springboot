package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 시험지 생성 요청 DTO
 */
@Schema(description = "시험지 생성 요청")
public record CreateExamSheetRequest(
    @NotBlank(message = "시험지 이름은 필수입니다")
    @Size(max = 100, message = "시험지 이름은 100자 이하여야 합니다")
    @Schema(description = "시험지 이름", example = "중간고사 문제지", requiredMode = Schema.RequiredMode.REQUIRED)
    String examName,
    
    @NotNull(message = "학년은 필수입니다")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @Max(value = 3, message = "학년은 3 이하여야 합니다")
    @Schema(description = "학년", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer grade,
    
    @NotEmpty(message = "문제 목록은 비어있을 수 없습니다")
    @Size(max = 50, message = "문제는 최대 50개까지 등록 가능합니다")
    @Schema(description = "문제 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<@Valid ExamQuestionRequest> questions
) {
    public CreateExamSheetRequest {
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(questions, "questions는 필수입니다");
        
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("최소 1개의 문제가 필요합니다");
        }
    }
    
    /**
     * 시험지에 포함될 문제 요청 DTO
     */
    @Schema(description = "시험지 문제 요청")
    public record ExamQuestionRequest(
        @NotNull(message = "문제 ID는 필수입니다")
        @Schema(description = "문제 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID questionId,
        
        @NotNull(message = "문제 순서는 필수입니다")
        @Min(value = 1, message = "문제 순서는 1 이상이어야 합니다")
        @Schema(description = "문제 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer questionOrder,
        
        @NotNull(message = "배점은 필수입니다")
        @Min(value = 1, message = "배점은 1점 이상이어야 합니다")
        @Max(value = 100, message = "배점은 100점 이하여야 합니다")
        @Schema(description = "배점", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer points
    ) {
        public ExamQuestionRequest {
            Objects.requireNonNull(questionId, "questionId는 필수입니다");
            Objects.requireNonNull(questionOrder, "questionOrder는 필수입니다");
            Objects.requireNonNull(points, "points는 필수입니다");
        }
    }
}