package com.iroomclass.springbackend.domain.admin.dashboard.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학년별 성적 분포도 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학년별 성적 분포도 응답")
public class GradeScoreDistributionResponse {

    @Schema(description = "학년", example = "1")
    private Integer grade;

    @Schema(description = "학년명", example = "중1")
    private String gradeName;

    @Schema(description = "전체 학생 수", example = "30")
    private int totalStudentCount;

    @Schema(description = "평균 성적이 있는 학생 수", example = "28")
    private int studentWithScoreCount;

    @Schema(description = "전체 평균 성적", example = "75.5")
    private double overallAverageScore;

    @Schema(description = "성적 구간별 분포")
    private List<ScoreRangeDistribution> scoreRanges;

    @Schema(description = "상위권/중위권/하위권 통계")
    private RankDistribution rankDistribution;

    /**
     * 성적 구간별 분포
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "성적 구간별 분포")
    public static class ScoreRangeDistribution {

        @Schema(description = "성적 구간", example = "90-100")
        private String scoreRange;

        @Schema(description = "학생 수", example = "5")
        private int studentCount;

        @Schema(description = "비율", example = "17.9")
        private double percentage;

        @Schema(description = "구간 시작 점수", example = "90")
        private int startScore;

        @Schema(description = "구간 끝 점수", example = "100")
        private int endScore;
    }

    /**
     * 상위권/중위권/하위권 통계
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상위권/중위권/하위권 통계")
    public static class RankDistribution {

        @Schema(description = "상위권 학생 수 (80점 이상)", example = "8")
        private int topRankCount;

        @Schema(description = "상위권 비율", example = "28.6")
        private double topRankPercentage;

        @Schema(description = "중위권 학생 수 (60-79점)", example = "12")
        private int middleRankCount;

        @Schema(description = "중위권 비율", example = "42.9")
        private double middleRankPercentage;

        @Schema(description = "하위권 학생 수 (60점 미만)", example = "8")
        private int bottomRankCount;

        @Schema(description = "하위권 비율", example = "28.6")
        private double bottomRankPercentage;
    }
}
