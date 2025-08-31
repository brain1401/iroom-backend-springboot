package com.iroomclass.springbackend.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 학년별 통계 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학년별 통계 응답")
public record GradeStatisticsResponse(
    @Schema(description = "학년", example = "1")
    Integer grade,

    @Schema(description = "학년명", example = "중1")
    String gradeName,

    @Schema(description = "최근 시험 평균 점수 목록")
    List<RecentExamAverage> recentExamAverages,

    @Schema(description = "오답률 높은 세부 단원 목록")
    List<HighErrorRateUnit> highErrorRateUnits
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public GradeStatisticsResponse {
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(gradeName, "gradeName는 필수입니다");
        Objects.requireNonNull(recentExamAverages, "recentExamAverages은 필수입니다");
        Objects.requireNonNull(highErrorRateUnits, "highErrorRateUnits은 필수입니다");
    }

    /**
     * 최근 시험 평균 점수
     */
    @Schema(description = "최근 시험 평균 점수")
    public record RecentExamAverage(
        @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID examId,

        @Schema(description = "시험명", example = "1학년 중간고사")
        String examName,

        @Schema(description = "평균 점수", example = "85.5")
        double averageScore,

        @Schema(description = "참여 학생 수", example = "25")
        int studentCount,

        @Schema(description = "시험 일시", example = "2024-03-15")
        String examDate
    ) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public RecentExamAverage {
            Objects.requireNonNull(examId, "examId은 필수입니다");
            Objects.requireNonNull(examName, "examName는 필수입니다");
            Objects.requireNonNull(examDate, "examDate는 필수입니다");
        }
    }

    /**
     * 오답률 높은 세부 단원
     */
    @Schema(description = "오답률 높은 세부 단원")
    public record HighErrorRateUnit(
        @Schema(description = "세부 단원 ID", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID unitId,

        @Schema(description = "세부 단원명", example = "정수")
        String unitName,

        @Schema(description = "오답률", example = "35.2")
        double errorRate,

        @Schema(description = "총 문제 수", example = "50")
        int totalQuestions,

        @Schema(description = "오답 수", example = "18")
        int incorrectAnswers,

        @Schema(description = "정답 수", example = "32")
        int correctAnswers
    ) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public HighErrorRateUnit {
            Objects.requireNonNull(unitId, "unitId은 필수입니다");
            Objects.requireNonNull(unitName, "unitName는 필수입니다");
        }
    }
}