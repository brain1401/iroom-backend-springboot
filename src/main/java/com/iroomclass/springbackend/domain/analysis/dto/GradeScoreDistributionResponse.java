package com.iroomclass.springbackend.domain.analysis.dto;

import java.util.List;
import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학년별 성적 분포도 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학년별 성적 분포도 응답")
public record GradeScoreDistributionResponse(
    @Schema(description = "학년", example = "1")
    Integer grade,

    @Schema(description = "학년명", example = "중1")
    String gradeName,

    @Schema(description = "전체 학생 수", example = "30")
    int totalStudentCount,

    @Schema(description = "평균 성적이 있는 학생 수", example = "28")
    int studentWithScoreCount,

    @Schema(description = "전체 평균 성적", example = "75.5")
    double overallAverageScore,

    @Schema(description = "성적 구간별 분포")
    List<ScoreRangeDistribution> scoreRanges,

    @Schema(description = "상위권/중위권/하위권 통계")
    RankDistribution rankDistribution
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public GradeScoreDistributionResponse {
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(gradeName, "gradeName는 필수입니다");
        Objects.requireNonNull(scoreRanges, "scoreRanges은 필수입니다");
        Objects.requireNonNull(rankDistribution, "rankDistribution은 필수입니다");
    }

    /**
     * 성적 구간별 분포
     */
    @Schema(description = "성적 구간별 분포")
    public record ScoreRangeDistribution(
        @Schema(description = "성적 구간", example = "90-100")
        String scoreRange,

        @Schema(description = "학생 수", example = "5")
        int studentCount,

        @Schema(description = "비율", example = "17.9")
        double percentage,

        @Schema(description = "구간 시작 점수", example = "90")
        int startScore,

        @Schema(description = "구간 끝 점수", example = "100")
        int endScore
    ) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public ScoreRangeDistribution {
            Objects.requireNonNull(scoreRange, "scoreRange는 필수입니다");
        }
    }

    /**
     * 상위권/중위권/하위권 통계
     */
    @Schema(description = "상위권/중위권/하위권 통계")
    public record RankDistribution(
        @Schema(description = "상위권 학생 수 (80점 이상)", example = "8")
        int topRankCount,

        @Schema(description = "상위권 비율", example = "28.6")
        double topRankPercentage,

        @Schema(description = "중위권 학생 수 (60-79점)", example = "12")
        int middleRankCount,

        @Schema(description = "중위권 비율", example = "42.9")
        double middleRankPercentage,

        @Schema(description = "하위권 학생 수 (60점 미만)", example = "8")
        int bottomRankCount,

        @Schema(description = "하위권 비율", example = "28.6")
        double bottomRankPercentage
    ) {
        /**
         * Compact Constructor - 기본 검증 수행 (primitive 타입이므로 null 체크 불필요)
         */
        public RankDistribution {
            // primitive 타입들은 null이 될 수 없으므로 별도 검증 불필요
        }
    }
}