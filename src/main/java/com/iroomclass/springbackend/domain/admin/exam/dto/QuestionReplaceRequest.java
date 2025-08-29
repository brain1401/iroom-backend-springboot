package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 문제 교체 요청 DTO
 * 
 * 문제 직접 선택 시스템에서 특정 문제를 다른 문제로 교체할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 문제 교체 요청")
public record QuestionReplaceRequest(
    @NotNull(message = "교체할 기존 문제 ID는 필수입니다")
    @Positive(message = "문제 ID는 양수여야 합니다")
    @Schema(description = "교체할 기존 문제 ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    Long oldQuestionId,
    
    @NotNull(message = "새로운 문제 ID는 필수입니다")
    @Positive(message = "문제 ID는 양수여야 합니다")
    @Schema(description = "새로운 문제 ID", example = "456", requiredMode = Schema.RequiredMode.REQUIRED)
    Long newQuestionId
) {
    public QuestionReplaceRequest {
        Objects.requireNonNull(oldQuestionId, "oldQuestionId는 필수입니다");
        Objects.requireNonNull(newQuestionId, "newQuestionId는 필수입니다");
        
        if (Objects.equals(oldQuestionId, newQuestionId)) {
            throw new IllegalArgumentException("기존 문제와 새로운 문제가 동일할 수 없습니다");
        }
    }
}