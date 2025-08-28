package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 답안 생성 요청 DTO
 * 
 * 학생이 답안 이미지를 업로드할 때 사용됩니다.
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
    
    @NotNull(message = "답안 이미지는 필수입니다.")
    @Schema(description = "답안 이미지 URL", example = "https://example.com/answer-image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    String answerImageUrl
) {
    public ExamAnswerCreateRequest {
        Objects.requireNonNull(examSubmissionId, "examSubmissionId은 필수입니다");
        Objects.requireNonNull(questionId, "questionId은 필수입니다");
        Objects.requireNonNull(answerImageUrl, "answerImageUrl은 필수입니다");
    }
}