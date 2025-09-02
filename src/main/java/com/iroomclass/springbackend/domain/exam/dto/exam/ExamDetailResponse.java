package com.iroomclass.springbackend.domain.exam.dto.exam;

import java.time.LocalDateTime;
import java.util.UUID;

import com.iroomclass.springbackend.common.BaseRecord;
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

        @Schema(description = "등록일시", example = "2024-06-01T12:34:56") LocalDateTime createdAt) implements BaseRecord {
    public ExamDetailResponse {
        requireAllNonNull(
            "examId", examId,
            "examSheetId", examSheetId,
            "examName", examName,
            "grade", grade,
            "studentCount", studentCount,
            "qrCodeUrl", qrCodeUrl,
            "createdAt", createdAt
        );
    }
}