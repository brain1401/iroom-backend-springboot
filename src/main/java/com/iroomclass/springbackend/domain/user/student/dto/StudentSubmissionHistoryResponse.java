package com.iroomclass.springbackend.domain.user.student.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학생 시험 제출 이력 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 시험 제출 이력 응답")
public class StudentSubmissionHistoryResponse {

    @Schema(description = "학생 이름", example = "김철수")
    private String studentName;

    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    private String studentPhone;

    @Schema(description = "시험 제출 이력 목록")
    private List<SubmissionInfo> submissions;

    @Schema(description = "총 제출 수", example = "5")
    private int totalCount;

    /**
     * 시험 제출 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "시험 제출 정보")
    public static class SubmissionInfo {

        @Schema(description = "시험 제출 ID", example = "1")
        private Long submissionId;

        @Schema(description = "시험 ID", example = "1")
        private Long examId;

        @Schema(description = "시험명", example = "1학년 중간고사")
        private String examName;

        @Schema(description = "학년", example = "1학년")
        private String grade;

        @Schema(description = "제출일시")
        private LocalDateTime submittedAt;

        @Schema(description = "총점", example = "85")
        private Integer totalScore;

        @Schema(description = "총 문제 수", example = "20")
        private int totalQuestions;

        @Schema(description = "정답 수", example = "17")
        private int correctCount;

        @Schema(description = "오답 수", example = "3")
        private int incorrectCount;

        @Schema(description = "정답률", example = "85.0")
        private double correctRate;
    }
}
