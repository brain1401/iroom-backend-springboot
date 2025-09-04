package com.iroomclass.springbackend.domain.unit.dto;

import com.iroomclass.springbackend.domain.unit.entity.Unit;
import com.iroomclass.springbackend.domain.unit.entity.UnitCategory;
import com.iroomclass.springbackend.domain.unit.entity.UnitSubcategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * 단원 트리 노드 DTO
 * 
 * 계층적 단원 구조 (대분류 → 중분류 → 세부단원)를 트리 형태로 표현합니다.
 */
@Schema(description = "단원 트리 노드")
public record UnitTreeNode(
    @Schema(description = "노드 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "노드 이름", example = "수와 연산")
    String name,
    
    @Schema(description = "노드 타입", example = "CATEGORY", allowableValues = {"CATEGORY", "SUBCATEGORY", "UNIT"})
    NodeType type,
    
    @Schema(description = "학년 (세부단원인 경우만 해당)", example = "1", nullable = true)
    Integer grade,
    
    @Schema(description = "표시 순서", example = "1")
    Integer displayOrder,
    
    @Schema(description = "설명", example = "수와 연산 관련 단원입니다", nullable = true)
    String description,
    
    @Schema(description = "단원 코드 (세부단원인 경우만)", example = "MS1_NUM_INT", nullable = true)
    String unitCode,
    
    @Schema(description = "하위 노드 목록")
    List<UnitTreeNode> children
) {
    
    /**
     * 노드 타입 열거형
     */
    public enum NodeType {
        @Schema(description = "대분류")
        CATEGORY,
        
        @Schema(description = "중분류") 
        SUBCATEGORY,
        
        @Schema(description = "세부단원")
        UNIT
    }
    
    /**
     * UnitCategory로부터 트리 노드 생성
     *
     * @param category 대분류 엔티티
     * @param children 하위 노드 목록
     * @return UnitTreeNode
     */
    public static UnitTreeNode fromCategory(UnitCategory category, List<UnitTreeNode> children) {
        return new UnitTreeNode(
            category.getId(),
            category.getCategoryName(),
            NodeType.CATEGORY,
            null, // 대분류는 학년이 없음
            category.getDisplayOrder(),
            category.getDescription(),
            null, // 대분류는 단원 코드가 없음
            children
        );
    }
    
    /**
     * UnitSubcategory로부터 트리 노드 생성
     *
     * @param subcategory 중분류 엔티티
     * @param children 하위 노드 목록
     * @return UnitTreeNode
     */
    public static UnitTreeNode fromSubcategory(UnitSubcategory subcategory, List<UnitTreeNode> children) {
        return new UnitTreeNode(
            subcategory.getId(),
            subcategory.getSubcategoryName(),
            NodeType.SUBCATEGORY,
            null, // 중분류는 학년이 없음
            subcategory.getDisplayOrder(),
            subcategory.getDescription(),
            null, // 중분류는 단원 코드가 없음
            children
        );
    }
    
    /**
     * Unit으로부터 트리 노드 생성 (리프 노드)
     *
     * @param unit 세부단원 엔티티
     * @return UnitTreeNode
     */
    public static UnitTreeNode fromUnit(Unit unit) {
        return new UnitTreeNode(
            unit.getId(),
            unit.getUnitName(),
            NodeType.UNIT,
            unit.getGrade(),
            unit.getDisplayOrder(),
            unit.getDescription(),
            unit.getUnitCode(),
            List.of() // 세부단원은 리프 노드이므로 하위 노드가 없음
        );
    }
    
    /**
     * 빈 루트 노드 생성 (특별한 경우)
     *
     * @param children 하위 노드 목록
     * @return UnitTreeNode
     */
    public static UnitTreeNode createRoot(List<UnitTreeNode> children) {
        return new UnitTreeNode(
            null,
            "전체",
            NodeType.CATEGORY,
            null,
            0,
            "전체 단원 구조",
            null,
            children
        );
    }
}