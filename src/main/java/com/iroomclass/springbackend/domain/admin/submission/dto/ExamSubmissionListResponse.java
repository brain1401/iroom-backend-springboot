package com.iroomclass.springbackend.domain.admin.submission.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자용 시험 제출 목록 응답 DTO
 * 
 * 관리자가 시험별 제출 현황을 조회할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "관리자용 시험 제출 목록 응답")
public record ExamSubmissionListResponse(
    @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID examId,
    
    @Schema(description = "시험명", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1학년")
    String grade,
    
    @Schema(description = "제출 목록")
    List<SubmissionInfo> submissions,
    
    @Schema(description = "총 제출 수", example = "25")
    int totalCount
) {
    public ExamSubmissionListResponse {
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(examName, "examName는 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(submissions, "submissions은 필수입니다");
    }
    
    /**
     * 제출 정보 내부 클래스
     */
    @Schema(description = "제출 정보")
    public record SubmissionInfo(
        @Schema(description = "제출 ID", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID submissionId,
        
        @Schema(description = "학생 이름", example = "김철수")
        String studentName,
        
        @Schema(description = "학생 전화번호", example = "010-1234-5678")
        String studentPhone,
        
        @Schema(description = "제출일시", example = "2024-06-01T12:34:56")
        LocalDateTime submittedAt,
        
        @Schema(description = "총점", example = "85", nullable = true)
        Integer totalScore
    ) {
        public SubmissionInfo {
            Objects.requireNonNull(submissionId, "submissionId은 필수입니다");
            Objects.requireNonNull(studentName, "studentName는 필수입니다");
            Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
            Objects.requireNonNull(submittedAt, "submittedAt은 필수입니다");
        }
    }
}