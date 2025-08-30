package com.iroomclass.springbackend.domain.admin.unit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 단원 통계 응답 DTO
 * 
 * 학년별 단원 통계 정보와 난이도별 분포를 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "단원 통계 응답")
public record UnitStatisticsResponse(
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "단원별 통계 목록")
    List<UnitStat> unitStats,
    
    @Schema(description = "전체 통계")
    TotalStat totalStat
) {
    public UnitStatisticsResponse {
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(unitStats, "unitStats은 필수입니다");
        Objects.requireNonNull(totalStat, "totalStat은 필수입니다");
    }
    
    /**
     * 단원별 통계
     */
    @Schema(description = "단원별 통계")
    public record UnitStat(
        @Schema(description = "단원 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID unitId,
        
        @Schema(description = "단원명", example = "자연수와 0")
        String unitName,
        
        @Schema(description = "전체 문제 수", example = "30")
        Integer totalQuestions,
        
        @Schema(description = "난이도별 문제 수")
        DifficultyCount difficultyCount
    ) {
        public UnitStat {
            Objects.requireNonNull(unitId, "unitId은 필수입니다");
            Objects.requireNonNull(unitName, "unitName는 필수입니다");
            Objects.requireNonNull(totalQuestions, "totalQuestions은 필수입니다");
            Objects.requireNonNull(difficultyCount, "difficultyCount은 필수입니다");
        }
    }
    
    /**
     * 난이도별 문제 수
     */
    @Schema(description = "난이도별 문제 수")
    public record DifficultyCount(
        @Schema(description = "쉬움 문제 수", example = "10")
        Integer easy,
        
        @Schema(description = "보통 문제 수", example = "15")
        Integer medium,
        
        @Schema(description = "어려움 문제 수", example = "5")
        Integer hard
    ) {
        public DifficultyCount {
            Objects.requireNonNull(easy, "easy은 필수입니다");
            Objects.requireNonNull(medium, "medium은 필수입니다");
            Objects.requireNonNull(hard, "hard은 필수입니다");
        }
    }
    
    /**
     * 전체 통계
     */
    @Schema(description = "전체 통계")
    public record TotalStat(
        @Schema(description = "전체 단원 수", example = "5")
        Integer totalUnits,
        
        @Schema(description = "전체 문제 수", example = "150")
        Integer totalQuestions,
        
        @Schema(description = "전체 난이도별 문제 수")
        DifficultyCount difficultyCount
    ) {
        public TotalStat {
            Objects.requireNonNull(totalUnits, "totalUnits은 필수입니다");
            Objects.requireNonNull(totalQuestions, "totalQuestions은 필수입니다");
            Objects.requireNonNull(difficultyCount, "difficultyCount은 필수입니다");
        }
    }
}