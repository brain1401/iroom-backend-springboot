package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;
import java.util.UUID;

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
    @Schema(description = "교체할 기존 문제 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID oldQuestionId,
    
    @NotNull(message = "새로운 문제 ID는 필수입니다")
    @Schema(description = "새로운 문제 ID", example = "550e8400-e29b-41d4-a716-446655440001", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID newQuestionId,
    
    @Positive(message = "배점은 양수여야 합니다")
    @Schema(description = "새 문제의 배점 (미지정 시 기존 배점 유지)", example = "5")
    Integer points,
    
    @Schema(description = "교체 사유 (선택사항)", example = "더 적절한 난이도의 문제로 교체")
    String reason
) {
    public QuestionReplaceRequest {
        Objects.requireNonNull(oldQuestionId, "oldQuestionId는 필수입니다");
        Objects.requireNonNull(newQuestionId, "newQuestionId는 필수입니다");
        
        if (Objects.equals(oldQuestionId, newQuestionId)) {
            throw new IllegalArgumentException("기존 문제와 새로운 문제가 동일할 수 없습니다");
        }
        
        if (reason != null && reason.length() > 200) {
            throw new IllegalArgumentException("교체 사유는 200자 이내여야 합니다");
        }
    }
}