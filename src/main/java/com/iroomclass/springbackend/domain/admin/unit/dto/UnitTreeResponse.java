package com.iroomclass.springbackend.domain.admin.unit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 단원 트리 구조 응답 DTO
 * 
 * 대분류 → 중분류 → 세부단원의 계층 구조를 표현합니다.
 * 문제 직접 선택 시스템에서 단원 트리 표시에 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "단원 트리 구조 응답")
public record UnitTreeResponse(
    @Schema(description = "대분류 ID", example = "1")
    Long categoryId,
    
    @Schema(description = "대분류명", example = "수와 연산")
    String categoryName,
    
    @Schema(description = "대분류 표시 순서", example = "1")
    Integer categoryDisplayOrder,
    
    @Schema(description = "대분류 설명", example = "정수, 유리수 등 수의 개념과 연산을 다룹니다")
    String categoryDescription,
    
    @Schema(description = "하위 중분류 목록")
    List<UnitSubcategoryNode> subcategories
) {
    
    /**
     * 중분류 노드
     */
    @Schema(description = "중분류 노드")
    public record UnitSubcategoryNode(
        @Schema(description = "중분류 ID", example = "1")
        Long subcategoryId,
        
        @Schema(description = "중분류명", example = "정수와 유리수")
        String subcategoryName,
        
        @Schema(description = "중분류 표시 순서", example = "1")
        Integer subcategoryDisplayOrder,
        
        @Schema(description = "중분류 설명", example = "정수와 유리수의 개념을 학습합니다")
        String subcategoryDescription,
        
        @Schema(description = "하위 세부단원 목록")
        List<UnitNode> units
    ) {}
    
    /**
     * 세부단원 노드
     */
    @Schema(description = "세부단원 노드")
    public record UnitNode(
        @Schema(description = "세부단원 ID", example = "1")
        Long unitId,
        
        @Schema(description = "세부단원명", example = "정수")
        String unitName,
        
        @Schema(description = "단원 코드", example = "MS1_NUM_INT")
        String unitCode,
        
        @Schema(description = "학년", example = "1")
        Integer grade,
        
        @Schema(description = "세부단원 표시 순서", example = "1")
        Integer unitDisplayOrder,
        
        @Schema(description = "세부단원 설명", example = "양수, 음수, 0을 포함한 정수의 개념을 학습합니다")
        String unitDescription
    ) {}
}