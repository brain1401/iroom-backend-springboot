package com.iroomclass.springbackend.domain.admin.question.dto;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제 목록 조회 응답 DTO
 * 
 * 단원별 문제 목록 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제 목록 조회 응답")
public record QuestionListResponse(
    @Schema(description = "단원 ID", example = "1")
    Long unitId,
    
    @Schema(description = "단원명", example = "자연수와 0")
    String unitName,
    
    @Schema(description = "문제 목록")
    List<QuestionInfo> questions,
    
    @Schema(description = "전체 문제 수", example = "30")
    int totalQuestions,
    
    @Schema(description = "쉬움 문제 수", example = "10")
    int easyCount,
    
    @Schema(description = "보통 문제 수", example = "15")
    int mediumCount,
    
    @Schema(description = "어려움 문제 수", example = "5")
    int hardCount
) {
    public QuestionListResponse {
        Objects.requireNonNull(unitId, "unitId은 필수입니다");
        Objects.requireNonNull(unitName, "unitName는 필수입니다");
        Objects.requireNonNull(questions, "questions은 필수입니다");
    }
    
    /**
     * 문제 정보
     */
    @Schema(description = "문제 정보")
    public record QuestionInfo(
        @Schema(description = "문제 ID", example = "1")
        Long questionId,
        
        @Schema(description = "난이도", example = "EASY", allowableValues = {"EASY", "MEDIUM", "HARD"})
        String difficulty
    ) {
        public QuestionInfo {
            Objects.requireNonNull(questionId, "questionId은 필수입니다");
            Objects.requireNonNull(difficulty, "difficulty은 필수입니다");
        }
    }
}