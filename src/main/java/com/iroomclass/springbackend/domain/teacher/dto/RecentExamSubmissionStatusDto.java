package com.iroomclass.springbackend.domain.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 최근 시험들 제출 현황 응답 DTO
 */
@Schema(description = "최근 시험들 제출 현황 응답")
public record RecentExamSubmissionStatusDto(
        @Schema(description = "학년", example = "1") Integer grade,

        @Schema(description = "조회된 시험 개수", example = "10") Integer examCount,

        @Schema(description = "전체 평균 제출률", example = "85.5") BigDecimal averageSubmissionRate,

        @Schema(description = "시험별 제출 현황") List<ExamSubmissionInfo> examSubmissions) {

    /**
     * 시험별 제출 정보
     */
    @Schema(description = "시험별 제출 정보")
    public record ExamSubmissionInfo(
            @Schema(description = "시험 ID") UUID examId,

            @Schema(description = "시험명", example = "1학기 중간고사") String examName,

            @Schema(description = "생성일시") LocalDateTime createdAt,

            @Schema(description = "최대 응시 가능 학생 수", example = "50") Integer maxStudent,

            @Schema(description = "실제 제출 수", example = "25") Long actualSubmissions,

            @Schema(description = "제출률 (%)", example = "83.33") BigDecimal submissionRate,

            @Schema(description = "문제 개수", example = "20") Integer questionCount) {
        /**
         * 제출률 계산하여 ExamSubmissionInfo 생성
         */
        public static ExamSubmissionInfo create(
                UUID examId,
                String examName,
                LocalDateTime createdAt,
                Integer maxStudent,
                Long actualSubmissions,
                Integer questionCount) {
            BigDecimal submissionRate = BigDecimal.ZERO;

            if (maxStudent > 0) {
                submissionRate = BigDecimal.valueOf(actualSubmissions)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(maxStudent), 2, RoundingMode.HALF_UP);
            }

            return new ExamSubmissionInfo(
                    examId,
                    examName,
                    createdAt,
                    maxStudent,
                    actualSubmissions,
                    submissionRate,
                    questionCount);
        }
    }

    /**
     * 평균 제출률 계산하여 RecentExamSubmissionStatusDto 생성
     */
    public static RecentExamSubmissionStatusDto create(
            Integer grade,
            List<ExamSubmissionInfo> examSubmissions) {
        // 전체 평균 제출률 계산
        BigDecimal averageSubmissionRate = BigDecimal.ZERO;

        if (!examSubmissions.isEmpty()) {
            BigDecimal totalSubmissionRate = examSubmissions.stream()
                    .map(ExamSubmissionInfo::submissionRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            averageSubmissionRate = totalSubmissionRate
                    .divide(BigDecimal.valueOf(examSubmissions.size()), 2, RoundingMode.HALF_UP);
        }

        return new RecentExamSubmissionStatusDto(
                grade,
                examSubmissions.size(),
                averageSubmissionRate,
                examSubmissions);
    }
}