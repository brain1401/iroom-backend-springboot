package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.UUID;

/**
 * 단원 요약 정보 DTO
 * 
 * <p>시험에 포함된 단원의 기본 정보와 계층 구조 정보를 제공합니다.
 * 성능 최적화를 위해 필요한 최소 정보만 포함하도록 설계되었습니다.</p>
 */
@Schema(description = "단원 요약 정보 DTO")
public record UnitSummaryDto(
    @Schema(description = "단원 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "단원명", example = "일차방정식")
    String unitName,
    
    @Schema(description = "단원 코드", example = "UNIT_001")
    String unitCode,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "단원 설명", example = "일차방정식의 기본 개념과 해법")
    String description,
    
    @Schema(description = "표시 순서", example = "1")
    Integer displayOrder,
    
    @Schema(description = "중분류 정보")
    UnitSubcategoryInfo subcategory,
    
    @Schema(description = "대분류 정보")
    UnitCategoryInfo category
) implements Serializable {

    /**
     * 중분류 정보
     */
    @Schema(description = "중분류 정보")
    public record UnitSubcategoryInfo(
        @Schema(description = "중분류 고유 식별자")
        UUID id,
        
        @Schema(description = "중분류명", example = "문자와 식")
        String subcategoryName,
        
        @Schema(description = "중분류 설명")
        String description,
        
        @Schema(description = "대분류 내 표시 순서")
        Integer displayOrder
    ) implements Serializable {}

    /**
     * 대분류 정보
     */
    @Schema(description = "대분류 정보")
    public record UnitCategoryInfo(
        @Schema(description = "대분류 고유 식별자")
        UUID id,
        
        @Schema(description = "대분류명", example = "수와 연산")
        String categoryName,
        
        @Schema(description = "대분류 설명")
        String description,
        
        @Schema(description = "표시 순서")
        Integer displayOrder
    ) implements Serializable {}

    /**
     * Compact Constructor - 필수 필드 검증
     */
    public UnitSummaryDto {
        if (id == null) throw new IllegalArgumentException("id는 필수입니다");
        if (unitName == null || unitName.isBlank()) throw new IllegalArgumentException("unitName은 필수입니다");
        if (unitCode == null || unitCode.isBlank()) throw new IllegalArgumentException("unitCode는 필수입니다");
        if (grade == null || grade < 1 || grade > 3) throw new IllegalArgumentException("grade는 1-3 사이여야 합니다");
        if (subcategory == null) throw new IllegalArgumentException("subcategory는 필수입니다");
        if (category == null) throw new IllegalArgumentException("category는 필수입니다");
    }

    /**
     * 단원의 전체 경로 생성 (대분류 > 중분류 > 단원)
     */
    public String getFullPath() {
        return String.format("%s > %s > %s", 
            category.categoryName(), 
            subcategory.subcategoryName(), 
            unitName);
    }

    /**
     * 단원의 계층 레벨 문자열 생성 (표시 순서 기반)
     */
    public String getHierarchyLevel() {
        return String.format("%d.%d.%d", 
            category.displayOrder(), 
            subcategory.displayOrder(), 
            displayOrder);
    }

    /**
     * 중분류 ID 조회
     */
    public UUID getSubcategoryId() {
        return subcategory.id();
    }

    /**
     * 대분류 ID 조회
     */
    public UUID getCategoryId() {
        return category.id();
    }

    /**
     * 같은 중분류에 속하는지 확인
     */
    public boolean belongsToSameSubcategory(UnitSummaryDto other) {
        return this.subcategory.id().equals(other.subcategory.id());
    }

    /**
     * 같은 대분류에 속하는지 확인
     */
    public boolean belongsToSameCategory(UnitSummaryDto other) {
        return this.category.id().equals(other.category.id());
    }
}