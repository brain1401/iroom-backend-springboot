package com.iroomclass.springbackend.domain.exam.dto.exam;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 상세 조회 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 상세 조회 응답")
public record ExamDetailResponse(
        @Schema(description = "시험 ID", example = "550e8400-e29b-41d4-a716-446655440000") UUID examId,

        @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440001") UUID examSheetId,

        @Schema(description = "시험명", example = "1학년 중간고사") String examName,

        @Schema(description = "학년", example = "1") Integer grade,

        @Schema(description = "시험 관련 메모/설명", example = "1학년 중간고사 - 자연수와 0 단원") String content,

        @Schema(description = "학생 수", example = "25") Integer studentCount,

        @Schema(description = "QR 코드 URL", example = "https://example.com/qr/123") String qrCodeUrl,

        @Schema(description = "등록일시", example = "2024-06-01T12:34:56") LocalDateTime createdAt) {
    public ExamDetailResponse {
        Objects.requireNonNull(examId, "examId는 필수입니다");
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(studentCount, "studentCount는 필수입니다");
        Objects.requireNonNull(qrCodeUrl, "qrCodeUrl은 필수입니다");
        Objects.requireNonNull(createdAt, "createdAt은 필수입니다");
    }
}