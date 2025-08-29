package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 답안 수정 요청 DTO
 * 
 * 학생이 주관식은 AI 인식 결과를 수정하고, 객관식은 선택지를 변경할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 답안 수정 요청")
public record ExamAnswerUpdateRequest(
        @NotNull(message = "답안 ID는 필수입니다.") @Schema(description = "답안 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long answerId,

        @Schema(description = "수정된 답안 텍스트 (주관식 문제용)", example = "x = 5") String answerText,

        @Schema(description = "선택한 답안 번호 (객관식 문제용)", example = "2") Integer selectedChoice) {
    public ExamAnswerUpdateRequest {
        Objects.requireNonNull(answerId, "answerId은 필수입니다");

        // 주관식과 객관식 중 하나는 반드시 있어야 함
        if ((answerText == null || answerText.isBlank()) && selectedChoice == null) {
            throw new IllegalArgumentException("주관식 답안 텍스트 또는 객관식 선택 답안 중 하나는 필수입니다");
        }

        // 동시에 둘 다 있으면 안됨 (명확성을 위해)
        if (answerText != null && !answerText.isBlank() && selectedChoice != null) {
            throw new IllegalArgumentException("주관식과 객관식 답안을 동시에 수정할 수 없습니다");
        }
    }
}