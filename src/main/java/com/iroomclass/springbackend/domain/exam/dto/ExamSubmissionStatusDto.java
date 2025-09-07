package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 상세 시험 제출 현황 응답 DTO
 */
@Schema(description = "상세 시험 제출 현황")
public record ExamSubmissionStatusDto(
        @Schema(description = "시험 기본 정보") ExamInfo examInfo,

        @Schema(description = "제출 현황 통계") SubmissionStats submissionStats,

        @Schema(description = "최근 제출자 목록 (최대 5명)") List<RecentSubmission> recentSubmissions,

        @Schema(description = "시간별 제출 통계") List<HourlyStats> hourlyStats) {

    /**
     * 시간별 제출 통계
     */
    @Schema(description = "시간별 제출 통계")
    public record HourlyStats(
            @Schema(description = "시간 (예: 2024-08-17 14:00:00)") String hour,

            @Schema(description = "해당 시간 제출 수", example = "15") Long submissionCount) {
        /**
         * Repository 결과로부터 DTO 생성
         */
        public static HourlyStats from(
                com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository.HourlySubmissionStats stats) {
            return new HourlyStats(stats.getHourGroup(), stats.getCount());
        }
    }

    /**
     * 제출 현황 통계 정보
     */
    @Schema(description = "제출 현황 통계")
    public record SubmissionStats(
            @Schema(description = "해당 문제에 배정된 최대 학생 수", example = "120") Long maxStudent,

            @Schema(description = "실제 제출자 수", example = "95") Long actualSubmissions,

            @Schema(description = "미제출자 수", example = "25") Long notSubmitted,

            @Schema(description = "제출률 (%)", example = "79.17") BigDecimal submissionRate,

            @Schema(description = "통계 생성 시간") LocalDateTime statsGeneratedAt) {
        /**
         * 제출 통계 생성
         * 
         * @param maxStudent        해당 문제에 배정된 최대 학생 수
         * @param actualSubmissions 실제 제출 수
         * @return 제출 통계
         */
        public static SubmissionStats create(Long maxStudent, Long actualSubmissions) {
            Long notSubmitted = maxStudent - actualSubmissions;
            BigDecimal submissionRate = BigDecimal.ZERO;

            if (maxStudent > 0) {
                submissionRate = BigDecimal.valueOf(actualSubmissions)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(maxStudent), 2, RoundingMode.HALF_UP);
            }

            return new SubmissionStats(
                    maxStudent,
                    actualSubmissions,
                    notSubmitted,
                    submissionRate,
                    LocalDateTime.now());
        }
    }

    /**
     * 최근 제출자 정보
     */
    @Schema(description = "최근 제출자 정보")
    public record RecentSubmission(
            @Schema(description = "제출 ID") UUID submissionId,

            @Schema(description = "학생 ID") Long studentId,

            @Schema(description = "학생 이름", example = "김철수") String studentName,

            @Schema(description = "제출 시간") LocalDateTime submittedAt) {
        /**
         * ExamSubmission으로부터 DTO 생성
         */
        public static RecentSubmission from(com.iroomclass.springbackend.domain.exam.entity.ExamSubmission submission) {
            return new RecentSubmission(
                    submission.getId(),
                    submission.getStudent().getId(),
                    submission.getStudent().getName(),
                    submission.getSubmittedAt());
        }
    }

    /**
     * 시험 기본 정보 (중복 방지를 위한 간소화된 정보)
     */
    @Schema(description = "시험 기본 정보")
    public record ExamInfo(
            @Schema(description = "시험 ID") UUID id,

            @Schema(description = "시험명", example = "중간고사") String examName,

            @Schema(description = "학년", example = "2") Integer grade,

            @Schema(description = "시험 내용/설명") String content,

            @Schema(description = "최대 학생 수", example = "30") Integer maxStudent,

            @Schema(description = "시험 생성일시") LocalDateTime createdAt,

            @Schema(description = "연결된 시험지 정보") ExamSheetSummary examSheetInfo) {
        /**
         * Exam으로부터 기본 정보 생성
         */
        public static ExamInfo from(com.iroomclass.springbackend.domain.exam.entity.Exam exam) {
            ExamSheetSummary examSheetSummary = null;
            if (exam.getExamSheet() != null) {
                examSheetSummary = ExamSheetSummary.from(exam.getExamSheet());
            }

            return new ExamInfo(
                    exam.getId(),
                    exam.getExamName(),
                    exam.getGrade(),
                    exam.getContent(),
                    exam.getMaxStudent(),
                    exam.getCreatedAt(),
                    examSheetSummary);
        }

        /**
         * 시험지 요약 정보
         */
        @Schema(description = "시험지 요약 정보")
        public record ExamSheetSummary(
                @Schema(description = "시험지 ID") UUID id,

                @Schema(description = "시험지 이름") String examName,

                @Schema(description = "총 문제 수", example = "20") Integer totalQuestions,

                @Schema(description = "총 배점", example = "100") Integer totalPoints) {
            public static ExamSheetSummary from(com.iroomclass.springbackend.domain.exam.entity.ExamSheet examSheet) {
                Integer totalQuestions = 0;
                Integer totalPoints = 0;

                if (examSheet.getQuestions() != null) {
                    totalQuestions = examSheet.getQuestions().size();
                    totalPoints = examSheet.getQuestions().stream()
                            .mapToInt(esq -> esq.getPoints())
                            .sum();
                }

                return new ExamSheetSummary(
                        examSheet.getId(),
                        examSheet.getExamName(),
                        totalQuestions,
                        totalPoints);
            }
        }
    }
}