package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 시험별 평균 점수 응답 DTO
 * 
 * <p>학년별 최근 시험들의 평균 점수와 통계를 보여주는 DTO입니다.
 * 각 시험의 응시 인원, 평균 점수, 점수 분포 등을 제공합니다.</p>
 */
@Schema(
    name = "ExamAverageScoreDto",
    description = "시험별 평균 점수 응답 DTO - 학년별 최근 시험들의 평균 점수 통계",
    example = """
        {
          "grade": 1,
          "totalExamCount": 15,
          "recentExamCount": 4,
          "overallAverageScore": 75.2,
          "examAverageScores": [
            {
              "examId": "01HKQR7T2M8C9F3G5H7J2K4L6N",
              "examName": "중간고사",
              "createdAt": "2024-08-15T10:00:00",
              "totalQuestionCount": 20,
              "participantCount": 28,
              "averageScore": 78.5,
              "maxScore": 95.0,
              "minScore": 45.0,
              "standardDeviation": 12.3,
              "submissionRate": 93.33
            }
          ],
          "statistics": {
            "highestExamAverage": 82.1,
            "lowestExamAverage": 68.9,
            "averageParticipationRate": 87.5,
            "totalParticipants": 110
          }
        }
        """
)
public record ExamAverageScoreDto(
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "해당 학년 전체 시험 개수", example = "15") 
    Long totalExamCount,
    
    @Schema(description = "조회된 최근 시험 개수", example = "4")
    Integer recentExamCount,
    
    @Schema(description = "전체 시험 평균 점수", example = "75.2")
    BigDecimal overallAverageScore,
    
    @Schema(description = "시험별 평균 점수 목록")
    List<ExamAverageScore> examAverageScores,
    
    @Schema(description = "통계 요약")
    StatisticsSummary statistics
) {

    /**
     * 시험별 평균 점수 응답 생성 팩토리 메서드
     * 
     * @param grade 학년
     * @param totalExamCount 전체 시험 개수
     * @param examAverageScores 시험별 평균 점수 목록
     * @return 시험별 평균 점수 응답 DTO
     */
    public static ExamAverageScoreDto create(
            Integer grade,
            Long totalExamCount,
            List<ExamAverageScore> examAverageScores) {
        
        int recentExamCount = examAverageScores.size();
        
        // 전체 평균 점수 계산
        BigDecimal overallAverageScore = examAverageScores.isEmpty() ? BigDecimal.ZERO :
            examAverageScores.stream()
                .map(ExamAverageScore::averageScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(recentExamCount), 2, RoundingMode.HALF_UP);
        
        // 통계 요약 생성
        StatisticsSummary statistics = createStatisticsSummary(examAverageScores);
        
        return new ExamAverageScoreDto(
            grade,
            totalExamCount,
            recentExamCount,
            overallAverageScore,
            examAverageScores,
            statistics
        );
    }

    /**
     * 통계 요약 생성
     */
    private static StatisticsSummary createStatisticsSummary(List<ExamAverageScore> examAverageScores) {
        if (examAverageScores.isEmpty()) {
            return new StatisticsSummary(
                BigDecimal.ZERO, 
                BigDecimal.ZERO, 
                BigDecimal.ZERO, 
                0L
            );
        }
        
        // 최고/최저 시험 평균
        BigDecimal highestExamAverage = examAverageScores.stream()
            .map(ExamAverageScore::averageScore)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
            
        BigDecimal lowestExamAverage = examAverageScores.stream()
            .map(ExamAverageScore::averageScore)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        // 평균 참여율 계산
        BigDecimal averageParticipationRate = examAverageScores.stream()
            .map(ExamAverageScore::submissionRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(examAverageScores.size()), 2, RoundingMode.HALF_UP);
        
        // 총 참여자 수
        Long totalParticipants = examAverageScores.stream()
            .mapToLong(ExamAverageScore::participantCount)
            .sum();
        
        return new StatisticsSummary(
            highestExamAverage,
            lowestExamAverage, 
            averageParticipationRate,
            totalParticipants
        );
    }

    /**
     * 시험별 평균 점수 정보
     */
    @Schema(
        name = "ExamAverageScore",
        description = "개별 시험의 평균 점수 정보"
    )
    public record ExamAverageScore(
        @Schema(description = "시험 ID")
        UUID examId,
        
        @Schema(description = "시험명", example = "중간고사")
        String examName,
        
        @Schema(description = "시험 생성일시", example = "2024-08-15T10:00:00")
        LocalDateTime createdAt,
        
        @Schema(description = "전체 문제 수", example = "20")
        Integer totalQuestionCount,
        
        @Schema(description = "응시 인원", example = "28")
        Long participantCount,
        
        @Schema(description = "평균 점수", example = "78.5")
        BigDecimal averageScore,
        
        @Schema(description = "최고 점수", example = "95.0")
        BigDecimal maxScore,
        
        @Schema(description = "최저 점수", example = "45.0")
        BigDecimal minScore,
        
        @Schema(description = "표준편차", example = "12.3")
        BigDecimal standardDeviation,
        
        @Schema(description = "제출률(%)", example = "93.33")
        BigDecimal submissionRate
    ) {
        
        /**
         * 시험별 평균 점수 정보 생성 팩토리 메서드
         * 
         * @param examId 시험 ID
         * @param examName 시험명
         * @param createdAt 시험 생성일시
         * @param totalQuestionCount 전체 문제 수
         * @param participantCount 응시 인원
         * @param averageScore 평균 점수
         * @param maxScore 최고 점수
         * @param minScore 최저 점수
         * @param standardDeviation 표준편차
         * @param totalStudentCount 해당 학년 전체 학생 수
         * @return 시험별 평균 점수 정보
         */
        public static ExamAverageScore create(
                UUID examId,
                String examName,
                LocalDateTime createdAt,
                Integer totalQuestionCount,
                Long participantCount,
                Double averageScore,
                Double maxScore,
                Double minScore,
                Double standardDeviation,
                Long totalStudentCount) {
            
            // 제출률 계산
            BigDecimal submissionRate = totalStudentCount > 0 
                ? BigDecimal.valueOf(participantCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalStudentCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            return new ExamAverageScore(
                examId,
                examName,
                createdAt,
                totalQuestionCount,
                participantCount,
                averageScore != null ? BigDecimal.valueOf(averageScore).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                maxScore != null ? BigDecimal.valueOf(maxScore).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                minScore != null ? BigDecimal.valueOf(minScore).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                standardDeviation != null ? BigDecimal.valueOf(standardDeviation).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                submissionRate
            );
        }
    }

    /**
     * 통계 요약 정보
     */
    @Schema(
        name = "StatisticsSummary",
        description = "시험별 평균 점수 통계 요약"
    )
    public record StatisticsSummary(
        @Schema(description = "가장 높은 시험 평균", example = "82.1")
        BigDecimal highestExamAverage,
        
        @Schema(description = "가장 낮은 시험 평균", example = "68.9")
        BigDecimal lowestExamAverage,
        
        @Schema(description = "평균 참여율(%)", example = "87.5")
        BigDecimal averageParticipationRate,
        
        @Schema(description = "총 참여자 수", example = "110")
        Long totalParticipants
    ) {}
}