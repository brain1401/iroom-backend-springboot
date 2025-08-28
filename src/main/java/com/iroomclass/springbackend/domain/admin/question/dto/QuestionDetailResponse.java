package com.iroomclass.springbackend.domain.admin.question.dto;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제 상세 조회 응답 DTO
 * 
 * 특정 문제의 상세 정보 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제 상세 조회 응답")
public record QuestionDetailResponse(
    @Schema(description = "문제 ID", example = "1")
    Long questionId,
    
    @Schema(description = "단원 ID", example = "1")
    Long unitId,
    
    @Schema(description = "단원명", example = "자연수와 0")
    String unitName,
    
    @Schema(description = "난이도", example = "EASY", allowableValues = {"EASY", "MEDIUM", "HARD"})
    String difficulty,
    
    @Schema(description = "문제 내용 (HTML)", example = "<p>다음 중 자연수는?</p>")
    String stem,
    
    @Schema(description = "정답", example = "A")
    String answerKey
) {
    public QuestionDetailResponse {
        Objects.requireNonNull(questionId, "questionId은 필수입니다");
        Objects.requireNonNull(unitId, "unitId은 필수입니다");
        Objects.requireNonNull(unitName, "unitName는 필수입니다");
        Objects.requireNonNull(difficulty, "difficulty은 필수입니다");
        Objects.requireNonNull(stem, "stem은 필수입니다");
        Objects.requireNonNull(answerKey, "answerKey은 필수입니다");
    }
}