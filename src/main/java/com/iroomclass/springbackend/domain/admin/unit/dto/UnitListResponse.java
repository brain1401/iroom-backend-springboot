package com.iroomclass.springbackend.domain.admin.unit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 단원 목록 응답 DTO
 * 
 * 학년별 단원 목록과 각 단원별 문제 수를 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "단원 목록 응답")
public record UnitListResponse(
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "단원 목록")
    List<UnitInfo> units,
    
    @Schema(description = "전체 단원 수", example = "5")
    Integer totalUnits,
    
    @Schema(description = "전체 문제 수", example = "150")
    Integer totalQuestions
) {
    public UnitListResponse {
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(units, "units은 필수입니다");
        Objects.requireNonNull(totalUnits, "totalUnits은 필수입니다");
        Objects.requireNonNull(totalQuestions, "totalQuestions은 필수입니다");
    }
    
    /**
     * 단원 정보
     */
    @Schema(description = "단원 정보")
    public record UnitInfo(
        @Schema(description = "단원 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID unitId,
        
        @Schema(description = "단원명", example = "자연수와 0")
        String unitName,
        
        @Schema(description = "단원 설명", example = "자연수와 0에 대한 기본 개념")
        String description,
        
        @Schema(description = "표시 순서", example = "1")
        Integer displayOrder,
        
        @Schema(description = "해당 단원의 문제 수", example = "30")
        Integer questionCount
    ) {
        public UnitInfo {
            Objects.requireNonNull(unitId, "unitId은 필수입니다");
            Objects.requireNonNull(unitName, "unitName는 필수입니다");
            Objects.requireNonNull(displayOrder, "displayOrder은 필수입니다");
            Objects.requireNonNull(questionCount, "questionCount은 필수입니다");
        }
    }
}