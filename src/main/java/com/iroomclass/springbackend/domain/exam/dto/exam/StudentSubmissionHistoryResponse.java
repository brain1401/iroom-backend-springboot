package com.iroomclass.springbackend.domain.exam.dto.exam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학생 시험 제출 이력 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 시험 제출 이력 응답")
public record StudentSubmissionHistoryResponse(
        @Schema(description = "학생 이름", example = "김철수") String studentName,

        @Schema(description = "학생 전화번호", example = "010-1234-5678") String studentPhone,

        @Schema(description = "시험 제출 이력 목록") List<SubmissionInfo> submissions,

        @Schema(description = "총 제출 수", example = "5") int totalCount) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public StudentSubmissionHistoryResponse {
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
        Objects.requireNonNull(submissions, "submissions은 필수입니다");
    }

    /**
     * 시험 제출 정보
     */
    @Schema(description = "시험 제출 정보")
    public record SubmissionInfo(
            @Schema(description = "시험 제출 ID", example = "550e8400-e29b-41d4-a716-446655440000") UUID submissionId,

            @Schema(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440001") UUID examId,

            @Schema(description = "시험명", example = "1학년 중간고사") String examName,

            @Schema(description = "학년", example = "1학년") String grade,

            @Schema(description = "제출일시") LocalDateTime submittedAt,

            @Schema(description = "총점", example = "85") Integer totalScore,

            @Schema(description = "총 문제 수", example = "20") int totalQuestions,

            @Schema(description = "정답 수", example = "17") int correctCount,

            @Schema(description = "오답 수", example = "3") int incorrectCount,

            @Schema(description = "정답률", example = "85.0") double correctRate) {
        /**
         * Compact Constructor - 입력 검증 수행
         */
        public SubmissionInfo {
            Objects.requireNonNull(submissionId, "submissionId은 필수입니다");
            Objects.requireNonNull(examId, "examId은 필수입니다");
            Objects.requireNonNull(examName, "examName는 필수입니다");
        }
    }
}