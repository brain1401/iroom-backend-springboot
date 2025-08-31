package com.iroomclass.springbackend.domain.exam.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자용 시험 제출 상세 응답 DTO
 * 
 * 관리자가 시험 제출 상세 정보를 조회할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "관리자용 시험 제출 상세 응답")
public record ExamSubmissionDetailResponse(
    @Schema(description = "제출 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID submissionId,
    
    @Schema(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID examId,
    
    @Schema(description = "시험명", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1학년")
    String grade,
    
    @Schema(description = "학생 이름", example = "김철수")
    String studentName,
    
    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    String studentPhone,
    
    @Schema(description = "제출일시", example = "2024-06-01T12:34:56")
    LocalDateTime submittedAt,
    
    @Schema(description = "총점", example = "85", nullable = true)
    Integer totalScore,
    
    @Schema(description = "QR 코드 URL", example = "https://example.com/qr/123")
    String qrCodeUrl
) {
    public ExamSubmissionDetailResponse {
        Objects.requireNonNull(submissionId, "submissionId은 필수입니다");
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(examName, "examName는 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
        Objects.requireNonNull(submittedAt, "submittedAt은 필수입니다");
        Objects.requireNonNull(qrCodeUrl, "qrCodeUrl은 필수입니다");
    }
}