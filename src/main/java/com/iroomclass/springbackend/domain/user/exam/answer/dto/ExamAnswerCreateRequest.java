package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 답안 생성 요청 DTO
 * 
 * 학생이 주관식은 답안 이미지를 업로드하고, 객관식은 선택지를 선택할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 답안 생성 요청")
public record ExamAnswerCreateRequest(
    @NotNull(message = "시험 제출 ID는 필수입니다.")
    @Schema(description = "시험 제출 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long examSubmissionId,
    
    @NotNull(message = "문제 ID는 필수입니다.")
    @Schema(description = "문제 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long questionId,
    
    @Schema(description = "답안 이미지 URL (주관식 문제용)", example = "https://example.com/answer-image.jpg")
    String answerImageUrl,
    
    @Schema(description = "선택한 답안 번호 (객관식 문제용)", example = "1")
    Integer selectedChoice
) {
    public ExamAnswerCreateRequest {
        Objects.requireNonNull(examSubmissionId, "examSubmissionId은 필수입니다");
        Objects.requireNonNull(questionId, "questionId은 필수입니다");
        
        // 주관식과 객관식 중 하나는 반드시 있어야 함
        if (answerImageUrl == null && selectedChoice == null) {
            throw new IllegalArgumentException("주관식 답안 이미지 또는 객관식 선택 답안 중 하나는 필수입니다");
        }
        
        // 동시에 둘 다 있으면 안됨 (명확성을 위해)
        if (answerImageUrl != null && selectedChoice != null) {
            throw new IllegalArgumentException("주관식과 객관식 답안을 동시에 제출할 수 없습니다");
        }
    }
}