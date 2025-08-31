package com.iroomclass.springbackend.domain.exam.dto.exam;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 등록 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 등록 응답")
public record ExamCreateResponse(
        @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID examId,

        @Schema(description = "시험명", example = "1학년 중간고사") String examName,

        @Schema(description = "학년", example = "1") Integer grade,

        @Schema(description = "학생 수", example = "25") Integer studentCount,

        @Schema(description = "QR 코드 URL", example = "https://example.com/qr/123") String qrCodeUrl,

        @Schema(description = "등록일시", example = "2024-06-01T12:34:56") LocalDateTime createdAt) {
    public ExamCreateResponse {
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(examName, "examName는 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(studentCount, "studentCount은 필수입니다");
        Objects.requireNonNull(qrCodeUrl, "qrCodeUrl은 필수입니다");
        Objects.requireNonNull(createdAt, "createdAt은 필수입니다");
    }
}