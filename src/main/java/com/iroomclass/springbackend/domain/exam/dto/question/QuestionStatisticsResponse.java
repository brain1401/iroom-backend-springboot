package com.iroomclass.springbackend.domain.exam.dto.question;

import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제 통계 조회 응답 DTO
 * 
 * 단원별 문제 통계 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "문제 통계 조회 응답")
public record QuestionStatisticsResponse(
    @Schema(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID unitId,
    
    @Schema(description = "단원명", example = "자연수와 0")
    String unitName,
    
    @Schema(description = "전체 문제 수", example = "30")
    int totalQuestions,
    
    @Schema(description = "쉬움 문제 수", example = "10")
    int easyCount,
    
    @Schema(description = "보통 문제 수", example = "15")
    int mediumCount,
    
    @Schema(description = "어려움 문제 수", example = "5")
    int hardCount
) {
    public QuestionStatisticsResponse {
        Objects.requireNonNull(unitId, "unitId은 필수입니다");
        Objects.requireNonNull(unitName, "unitName는 필수입니다");
    }
}