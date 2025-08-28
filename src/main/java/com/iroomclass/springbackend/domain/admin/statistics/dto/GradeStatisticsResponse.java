package com.iroomclass.springbackend.domain.admin.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "학년별 통계 응답")
public class GradeStatisticsResponse {

    @Schema(description = "학년", example = "1")
    private Integer grade;

    @Schema(description = "학년명", example = "중1")
    private String gradeName;

    @Schema(description = "최근 시험 평균 점수 목록")
    private List<RecentExamAverage> recentExamAverages;

    @Schema(description = "오답률 높은 세부 단원 목록")
    private List<HighErrorRateUnit> highErrorRateUnits;

    @Getter
    @Setter
    @Builder
    @Schema(description = "최근 시험 평균 점수")
    public static class RecentExamAverage {
        @Schema(description = "시험 ID", example = "1")
        private Long examId;

        @Schema(description = "시험명", example = "1학년 중간고사")
        private String examName;

        @Schema(description = "평균 점수", example = "85.5")
        private double averageScore;

        @Schema(description = "참여 학생 수", example = "25")
        private int studentCount;

        @Schema(description = "시험 일시", example = "2024-03-15")
        private String examDate;
    }

    @Getter
    @Setter
    @Builder
    @Schema(description = "오답률 높은 세부 단원")
    public static class HighErrorRateUnit {
        @Schema(description = "세부 단원 ID", example = "1")
        private Long unitId;

        @Schema(description = "세부 단원명", example = "정수")
        private String unitName;

        @Schema(description = "오답률", example = "35.2")
        private double errorRate;

        @Schema(description = "총 문제 수", example = "50")
        private int totalQuestions;

        @Schema(description = "오답 수", example = "18")
        private int incorrectAnswers;

        @Schema(description = "정답 수", example = "32")
        private int correctAnswers;
    }
}
