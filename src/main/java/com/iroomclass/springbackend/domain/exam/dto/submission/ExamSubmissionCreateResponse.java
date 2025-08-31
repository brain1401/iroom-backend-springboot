package com.iroomclass.springbackend.domain.exam.dto.submission;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 제출 생성 응답 DTO
 * 
 * 시험 제출 완료 후 반환되는 정보입니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 제출 생성 응답")
public record ExamSubmissionCreateResponse(
        @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID submissionId,

        @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174001") UUID examId,

        @Schema(description = "시험 이름", example = "1학년 중간고사") String examName,

        @Schema(description = "학생 이름", example = "김철수") String studentName,

        @Schema(description = "학생 전화번호", example = "010-1234-5678") String studentPhone,

        @Schema(description = "제출일시", example = "2024-06-01T12:34:56") LocalDateTime submittedAt,

        @Schema(description = "QR 코드 URL (시험 접속용)", example = "https://example.com/qr/123") String qrCodeUrl) {
    public ExamSubmissionCreateResponse {
        Objects.requireNonNull(submissionId, "submissionId은 필수입니다");
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(examName, "examName는 필수입니다");
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
        Objects.requireNonNull(submittedAt, "submittedAt은 필수입니다");
        Objects.requireNonNull(qrCodeUrl, "qrCodeUrl은 필수입니다");
    }
}