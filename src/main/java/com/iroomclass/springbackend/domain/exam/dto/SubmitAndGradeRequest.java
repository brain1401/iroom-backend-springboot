package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

/**
 * 시험 답안 제출 및 채점 요청 DTO
 * 
 * <p>학생이 시험 답안을 제출하고 즉시 채점을 요청하는 통합 요청 DTO입니다.</p>
 */
@Schema(description = "시험 답안 제출 및 채점 요청")
public record SubmitAndGradeRequest(
    @NotNull(message = "시험 ID는 필수입니다")
    @Schema(description = "시험 ID", example = "018e3d5a-7b4c-7000-8000-000000000000", required = true)
    UUID examId,
    
    @NotNull(message = "학생 ID는 필수입니다")
    @Positive(message = "학생 ID는 양수여야 합니다")
    @Schema(description = "학생 ID", example = "12345", required = true)
    Long studentId,
    
    @NotEmpty(message = "답안은 필수입니다")
    @Valid
    @Schema(description = "문제별 답안 목록", required = true)
    List<AnswerDto> answers,
    
    @Schema(description = "즉시 채점 여부", example = "true", defaultValue = "true")
    Boolean forceGrading,
    
    @Schema(description = "채점 옵션 (선택사항)")
    Map<String, Object> gradingOptions
) {
    /**
     * Compact constructor로 null 검증 및 기본값 설정
     */
    public SubmitAndGradeRequest {
        Objects.requireNonNull(examId, "examId는 null일 수 없습니다");
        Objects.requireNonNull(studentId, "studentId는 null일 수 없습니다");
        Objects.requireNonNull(answers, "answers는 null일 수 없습니다");
        
        // 기본값 설정
        if (forceGrading == null) {
            forceGrading = true;
        }
        
        if (gradingOptions == null) {
            gradingOptions = Map.of();
        }
    }
    
    /**
     * 답안 DTO
     * 
     * <p>개별 문제에 대한 답안 정보를 담는 DTO입니다.</p>
     */
    @Schema(description = "문제별 답안")
    public record AnswerDto(
        @NotNull(message = "문제 ID는 필수입니다")
        @Schema(description = "문제 ID", example = "018e3d5a-7b4c-7000-8000-000000000001", required = true)
        UUID questionId,
        
        @Schema(description = "선택한 보기 번호 (객관식)", example = "3")
        Integer selectedChoice,
        
        @Schema(description = "답안 텍스트 (주관식)", example = "x = 10, y = 20")
        String answerText
    ) {
        /**
         * Compact constructor로 검증
         */
        public AnswerDto {
            Objects.requireNonNull(questionId, "questionId는 null일 수 없습니다");
            
            // 객관식이면 selectedChoice가 있어야 하고, 주관식이면 answerText가 있어야 함
            if (selectedChoice == null && (answerText == null || answerText.isBlank())) {
                throw new IllegalArgumentException("답안은 selectedChoice 또는 answerText 중 하나가 필수입니다");
            }
            
            if (selectedChoice != null && answerText != null && !answerText.isBlank()) {
                throw new IllegalArgumentException("답안은 selectedChoice 또는 answerText 중 하나만 있어야 합니다");
            }
        }
    }
}