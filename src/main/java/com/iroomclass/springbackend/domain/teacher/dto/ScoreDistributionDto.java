package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 성적 분포도 응답 DTO
 * 
 * <p>전체 학생들의 평균 성적을 구간별로 나누어 분포를 보여주는 DTO입니다.
 * 10점 단위 구간으로 나누어 통계를 제공합니다.</p>
 */
@Schema(
    name = "ScoreDistributionDto",
    description = "성적 분포도 응답 DTO - 전체 학생 평균 성적의 구간별 분포",
    example = """
        {
          "grade": 1,
          "totalStudentCount": 120,
          "averageScore": 72.5,
          "standardDeviation": 15.2,
          "medianScore": 75.0,
          "distributions": [
            {
              "scoreRange": "70-79점",
              "rangeMin": 70,
              "rangeMax": 79,
              "studentCount": 35,
              "percentage": 29.17
            }
          ],
          "statistics": {
            "maxScore": 98.5,
            "minScore": 45.2,
            "passingRate": 85.83,
            "excellentRate": 25.0
          }
        }
        """
)
public record ScoreDistributionDto(
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "전체 학생 수", example = "120")
    Long totalStudentCount,
    
    @Schema(description = "전체 평균 점수", example = "72.5")
    BigDecimal averageScore,
    
    @Schema(description = "표준편차", example = "15.2") 
    BigDecimal standardDeviation,
    
    @Schema(description = "중앙값", example = "75.0")
    BigDecimal medianScore,
    
    @Schema(description = "구간별 분포 목록")
    List<ScoreDistribution> distributions,
    
    @Schema(description = "통계 요약")
    StatisticsSummary statistics
) {

    /**
     * 성적 분포도 생성 팩토리 메서드
     * 
     * @param grade 학년
     * @param totalStudentCount 전체 학생 수
     * @param averageScore 평균 점수
     * @param standardDeviation 표준편차
     * @param medianScore 중앙값
     * @param distributions 구간별 분포 목록
     * @param maxScore 최고 점수
     * @param minScore 최저 점수
     * @return 성적 분포도 DTO
     */
    public static ScoreDistributionDto create(
            Integer grade,
            Long totalStudentCount, 
            BigDecimal averageScore,
            BigDecimal standardDeviation,
            BigDecimal medianScore,
            List<ScoreDistribution> distributions,
            BigDecimal maxScore,
            BigDecimal minScore) {
        
        // 합격률 계산 (60점 이상)
        long passingCount = distributions.stream()
            .filter(dist -> dist.rangeMin() >= 60)
            .mapToLong(ScoreDistribution::studentCount)
            .sum();
        
        BigDecimal passingRate = totalStudentCount > 0 
            ? BigDecimal.valueOf(passingCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalStudentCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // 우수율 계산 (80점 이상)
        long excellentCount = distributions.stream()
            .filter(dist -> dist.rangeMin() >= 80)
            .mapToLong(ScoreDistribution::studentCount)
            .sum();
        
        BigDecimal excellentRate = totalStudentCount > 0
            ? BigDecimal.valueOf(excellentCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalStudentCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        StatisticsSummary statistics = new StatisticsSummary(
            maxScore != null ? maxScore : BigDecimal.ZERO,
            minScore != null ? minScore : BigDecimal.ZERO,
            passingRate,
            excellentRate
        );
        
        return new ScoreDistributionDto(
            grade,
            totalStudentCount,
            averageScore != null ? averageScore : BigDecimal.ZERO,
            standardDeviation != null ? standardDeviation : BigDecimal.ZERO,
            medianScore != null ? medianScore : BigDecimal.ZERO,
            distributions,
            statistics
        );
    }

    /**
     * 구간별 성적 분포 정보
     */
    @Schema(
        name = "ScoreDistribution", 
        description = "구간별 성적 분포 정보"
    )
    public record ScoreDistribution(
        @Schema(description = "점수 구간명", example = "70-79점")
        String scoreRange,
        
        @Schema(description = "구간 최소값", example = "70")
        Integer rangeMin,
        
        @Schema(description = "구간 최대값", example = "79") 
        Integer rangeMax,
        
        @Schema(description = "해당 구간 학생 수", example = "35")
        Long studentCount,
        
        @Schema(description = "전체 대비 비율(%)", example = "29.17")
        BigDecimal percentage
    ) {
        
        /**
         * 구간별 분포 정보 생성 팩토리 메서드
         * 
         * @param rangeMin 구간 최소값
         * @param rangeMax 구간 최대값  
         * @param studentCount 해당 구간 학생 수
         * @param totalCount 전체 학생 수
         * @return 구간별 분포 정보
         */
        public static ScoreDistribution create(
                Integer rangeMin, 
                Integer rangeMax, 
                Long studentCount, 
                Long totalCount) {
            
            String scoreRange = rangeMin + "-" + rangeMax + "점";
            
            BigDecimal percentage = totalCount > 0 
                ? BigDecimal.valueOf(studentCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            return new ScoreDistribution(
                scoreRange,
                rangeMin,
                rangeMax,
                studentCount,
                percentage
            );
        }
    }

    /**
     * 통계 요약 정보
     */
    @Schema(
        name = "StatisticsSummary",
        description = "통계 요약 정보"
    )
    public record StatisticsSummary(
        @Schema(description = "최고 점수", example = "98.5")
        BigDecimal maxScore,
        
        @Schema(description = "최저 점수", example = "45.2")
        BigDecimal minScore,
        
        @Schema(description = "합격률 (60점 이상, %)", example = "85.83")
        BigDecimal passingRate,
        
        @Schema(description = "우수율 (80점 이상, %)", example = "25.0") 
        BigDecimal excellentRate
    ) {}

    /**
     * 표준 점수 구간 정의
     * 0-39점(낙제), 40-59점(미달), 60-69점(보통), 70-79점(양호), 80-89점(우수), 90-100점(최우수)
     */
    public static List<ScoreRange> getStandardScoreRanges() {
        return List.of(
            new ScoreRange(0, 39, "낙제"),
            new ScoreRange(40, 59, "미달"), 
            new ScoreRange(60, 69, "보통"),
            new ScoreRange(70, 79, "양호"),
            new ScoreRange(80, 89, "우수"),
            new ScoreRange(90, 100, "최우수")
        );
    }

    /**
     * 점수 구간 정의
     */
    public record ScoreRange(
        Integer min,
        Integer max, 
        String label
    ) {}
}