package com.iroomclass.springbackend.domain.unit.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 단원 통계 응답 DTO
 * 
 * 학년별 단원 통계 정보와 난이도별 분포를 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitStatisticsResponse {
    
    /**
     * 학년
     */
    private Integer grade;
    
    /**
     * 단원별 통계 목록
     */
    private List<UnitStat> unitStats;
    
    /**
     * 전체 통계
     */
    private TotalStat totalStat;
    
    /**
     * 단원별 통계
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitStat {
        
        /**
         * 단원 ID
         */
        private Long unitId;
        
        /**
         * 단원명
         */
        private String unitName;
        
        /**
         * 전체 문제 수
         */
        private Integer totalQuestions;
        
        /**
         * 난이도별 문제 수
         */
        private DifficultyCount difficultyCount;
    }
    
    /**
     * 난이도별 문제 수
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyCount {
        
        /**
         * 쉬움 문제 수
         */
        private Integer easy;
        
        /**
         * 보통 문제 수
         */
        private Integer medium;
        
        /**
         * 어려움 문제 수
         */
        private Integer hard;
    }
    
    /**
     * 전체 통계
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalStat {
        
        /**
         * 전체 단원 수
         */
        private Integer totalUnits;
        
        /**
         * 전체 문제 수
         */
        private Integer totalQuestions;
        
        /**
         * 전체 난이도별 문제 수
         */
        private DifficultyCount difficultyCount;
    }
}