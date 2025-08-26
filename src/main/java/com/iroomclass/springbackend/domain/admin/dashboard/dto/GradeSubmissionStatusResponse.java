package com.iroomclass.springbackend.domain.admin.dashboard.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학년별 시험 제출 현황 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학년별 시험 제출 현황 응답")
public class GradeSubmissionStatusResponse {

    @Schema(description = "학년", example = "1")
    private Integer grade;

    @Schema(description = "학년명", example = "중1")
    private String gradeName;

    @Schema(description = "시험별 제출 현황 목록")
    private List<ExamSubmissionStatus> examSubmissions;

    @Schema(description = "총 시험 수", example = "5")
    private int totalExamCount;

    @Schema(description = "총 제출 수", example = "150")
    private int totalSubmissionCount;

    /**
     * 시험별 제출 현황
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "시험별 제출 현황")
    public static class ExamSubmissionStatus {

        @Schema(description = "시험 ID", example = "1")
        private Long examId;

        @Schema(description = "시험명", example = "1학년 중간고사")
        private String examName;

        @Schema(description = "전체 학생 수", example = "30")
        private int totalStudentCount;

        @Schema(description = "제출한 학생 수", example = "28")
        private int submittedStudentCount;

        @Schema(description = "제출률", example = "93.3")
        private double submissionRate;

        @Schema(description = "미제출 학생 수", example = "2")
        private int notSubmittedStudentCount;
    }
}
