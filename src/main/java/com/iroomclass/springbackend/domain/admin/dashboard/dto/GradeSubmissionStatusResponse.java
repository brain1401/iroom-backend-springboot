package com.iroomclass.springbackend.domain.admin.dashboard.dto;

import java.util.List;
import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학년별 시험 제출 현황 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학년별 시험 제출 현황 응답")
public record GradeSubmissionStatusResponse(
    @Schema(description = "학년", example = "1")
    Integer grade,

    @Schema(description = "학년명", example = "중1")
    String gradeName,

    @Schema(description = "시험별 제출 현황 목록")
    List<ExamSubmissionStatus> examSubmissions,

    @Schema(description = "총 시험 수", example = "5")
    int totalExamCount,

    @Schema(description = "총 제출 수", example = "150")
    int totalSubmissionCount
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public GradeSubmissionStatusResponse {
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(gradeName, "gradeName는 필수입니다");
        Objects.requireNonNull(examSubmissions, "examSubmissions은 필수입니다");
    }

    /**
     * 시험별 제출 현황
     */
    @Schema(description = "시험별 제출 현황")
    public record ExamSubmissionStatus(
        @Schema(description = "시험 ID", example = "1")
        Long examId,

        @Schema(description = "시험명", example = "1학년 중간고사")
        String examName,

        @Schema(description = "전체 학생 수", example = "30")
        int totalStudentCount,

        @Schema(description = "제출한 학생 수", example = "28")
        int submittedStudentCount,

        @Schema(description = "제출률", example = "93.3")
        double submissionRate,

        @Schema(description = "미제출 학생 수", example = "2")
        int notSubmittedStudentCount
    ) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public ExamSubmissionStatus {
            Objects.requireNonNull(examId, "examId은 필수입니다");
            Objects.requireNonNull(examName, "examName는 필수입니다");
        }
    }
}