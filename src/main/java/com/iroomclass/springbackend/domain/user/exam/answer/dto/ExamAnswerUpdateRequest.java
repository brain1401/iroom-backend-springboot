package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 답안 수정 요청 DTO
 * 
 * 학생이 AI 인식 결과를 수정할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 답안 수정 요청")
public record ExamAnswerUpdateRequest(
    @NotNull(message = "답안 ID는 필수입니다.")
    @Schema(description = "답안 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long answerId,
    
    @NotBlank(message = "답안 내용은 필수입니다.")
    @Schema(description = "수정된 답안 텍스트", example = "x = 5", requiredMode = Schema.RequiredMode.REQUIRED)
    String answerText
) {
    public ExamAnswerUpdateRequest {
        Objects.requireNonNull(answerId, "answerId은 필수입니다");
        Objects.requireNonNull(answerText, "answerText은 필수입니다");
    }
}