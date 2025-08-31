package com.iroomclass.springbackend.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

/**
 * 전체 학년 통합 통계 응답 DTO
 * 
 * 모든 학년의 통합 통계 정보를 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "전체 학년 통합 통계 응답")
public record OverallStatisticsResponse(
    @Schema(description = "전체 학생 수", example = "150")
    int totalStudentCount,
    
    @Schema(description = "전체 평균 성적", example = "75.8")
    double overallAverageScore,
    
    @Schema(description = "상/중/하위권 분포")
    OverallRankDistribution rankDistribution,
    
    @Schema(description = "학년별 세부 통계")
    List<GradeStatistics> gradeStatistics
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public OverallStatisticsResponse {
        Objects.requireNonNull(rankDistribution, "rankDistribution은 필수입니다");
        Objects.requireNonNull(gradeStatistics, "gradeStatistics는 필수입니다");
    }
    
    /**
     * 상/중/하위권 분포 정보
     */
    @Schema(description = "상/중/하위권 분포")
    public record OverallRankDistribution(
        @Schema(description = "상위권 학생 수 (80점 이상)", example = "45")
        int highRankCount,
        
        @Schema(description = "중위권 학생 수 (60-79점)", example = "75")
        int middleRankCount,
        
        @Schema(description = "하위권 학생 수 (60점 미만)", example = "30")
        int lowRankCount,
        
        @Schema(description = "상위권 비율 (%)", example = "30.0")
        double highRankPercentage,
        
        @Schema(description = "중위권 비율 (%)", example = "50.0")
        double middleRankPercentage,
        
        @Schema(description = "하위권 비율 (%)", example = "20.0")
        double lowRankPercentage
    ) {}
    
    /**
     * 학년별 통계 정보
     */
    @Schema(description = "학년별 통계")
    public record GradeStatistics(
        @Schema(description = "학년", example = "1")
        int grade,
        
        @Schema(description = "해당 학년 학생 수", example = "50")
        int studentCount,
        
        @Schema(description = "해당 학년 평균 성적", example = "78.5")
        double averageScore,
        
        @Schema(description = "해당 학년 최고점", example = "95")
        int maxScore,
        
        @Schema(description = "해당 학년 최저점", example = "42")
        int minScore
    ) {}
}