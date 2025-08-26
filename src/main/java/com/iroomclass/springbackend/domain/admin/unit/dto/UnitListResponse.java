package com.iroomclass.springbackend.domain.admin.unit.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 단원 목록 응답 DTO
 * 
 * 학년별 단원 목록과 각 단원별 문제 수를 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitListResponse {
    
    /**
     * 학년
     */
    private Integer grade;
    
    /**
     * 단원 목록
     */
    private List<UnitInfo> units;
    
    /**
     * 전체 단원 수
     */
    private Integer totalUnits;
    
    /**
     * 전체 문제 수
     */
    private Integer totalQuestions;
    
    /**
     * 단원 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitInfo {
        
        /**
         * 단원 ID
         */
        private Long unitId;
        
        /**
         * 단원명
         */
        private String unitName;
        
        /**
         * 단원 설명
         */
        private String description;
        
        /**
         * 표시 순서
         */
        private Integer displayOrder;
        
        /**
         * 해당 단원의 문제 수
         */
        private Integer questionCount;
    }
}