package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * 단원별 오답률 응답 DTO
 */
@Schema(
    name = "UnitWrongAnswerRateDto", 
    description = "단원별 오답률 통계 정보",
    example = """
        {
          "grade": 2,
          "totalQuestionCount": 125,
          "totalSubmissionCount": 450,
          "overallWrongAnswerRate": 35.2,
          "unitStatistics": [
            {
              "unitId": "550e8400-e29b-41d4-a716-446655440000",
              "unitName": "일차방정식",
              "categoryName": "문자와 식",
              "subcategoryName": "방정식",
              "questionCount": 25,
              "submissionCount": 90,
              "wrongAnswerCount": 32,
              "wrongAnswerRate": 35.6,
              "rank": 1
            }
          ]
        }
        """
)
public record UnitWrongAnswerRateDto(
    @Schema(description = "학년", example = "2")
    Integer grade,
    
    @Schema(description = "전체 문제 수", example = "125") 
    Long totalQuestionCount,
    
    @Schema(description = "전체 제출 수", example = "450")
    Long totalSubmissionCount,
    
    @Schema(description = "전체 오답률 (%)", example = "35.2")
    BigDecimal overallWrongAnswerRate,
    
    @Schema(description = "단원별 오답률 통계 목록")
    List<UnitStatistic> unitStatistics
) {
    
    /**
     * 단원별 오답률 통계
     */
    @Schema(name = "UnitStatistic", description = "단원별 오답률 통계 정보")
    public record UnitStatistic(
        @Schema(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID unitId,
        
        @Schema(description = "단원명", example = "일차방정식")
        String unitName,
        
        @Schema(description = "대분류명", example = "문자와 식")
        String categoryName,
        
        @Schema(description = "중분류명", example = "방정식")
        String subcategoryName,
        
        @Schema(description = "해당 단원 문제 수", example = "25")
        Long questionCount,
        
        @Schema(description = "해당 단원 제출 수", example = "90")
        Long submissionCount,
        
        @Schema(description = "오답 수", example = "32")
        Long wrongAnswerCount,
        
        @Schema(description = "오답률 (%)", example = "35.6")
        BigDecimal wrongAnswerRate,
        
        @Schema(description = "오답률 순위 (높은 순)", example = "1")
        Integer rank
    ) {
        /**
         * 단원 오답률 통계 생성
         */
        public static UnitStatistic create(
            UUID unitId,
            String unitName,
            String categoryName,  
            String subcategoryName,
            Long questionCount,
            Long submissionCount,
            Long wrongAnswerCount,
            Integer rank
        ) {
            BigDecimal wrongAnswerRate = BigDecimal.ZERO;
            
            if (submissionCount > 0) {
                wrongAnswerRate = BigDecimal.valueOf(wrongAnswerCount)
                    .divide(BigDecimal.valueOf(submissionCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
            }
            
            return new UnitStatistic(
                unitId,
                unitName,
                categoryName,
                subcategoryName,
                questionCount,
                submissionCount,
                wrongAnswerCount,
                wrongAnswerRate,
                rank
            );
        }
    }
    
    /**
     * 단원별 오답률 DTO 생성 (빈 데이터)
     */
    public static UnitWrongAnswerRateDto empty(Integer grade) {
        return new UnitWrongAnswerRateDto(
            grade,
            0L,
            0L,
            BigDecimal.ZERO,
            List.of()
        );
    }
    
    /**
     * 단원별 오답률 DTO 생성
     */
    public static UnitWrongAnswerRateDto create(
        Integer grade,
        Long totalQuestionCount,
        Long totalSubmissionCount,
        Long totalWrongAnswerCount,
        List<UnitStatistic> unitStatistics
    ) {
        BigDecimal overallWrongAnswerRate = BigDecimal.ZERO;
        
        if (totalSubmissionCount > 0) {
            overallWrongAnswerRate = BigDecimal.valueOf(totalWrongAnswerCount)
                .divide(BigDecimal.valueOf(totalSubmissionCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
        }
        
        return new UnitWrongAnswerRateDto(
            grade,
            totalQuestionCount,
            totalSubmissionCount,
            overallWrongAnswerRate,
            unitStatistics
        );
    }
}