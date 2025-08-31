package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 문서 상세 조회 응답 DTO
 * 
 * 시험지 문서 상세 정보 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 문서 상세 조회 응답")
public record ExamDocumentDetailResponse(
    @Schema(description = "문서 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID documentId,
    
    @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID examSheetId,
    
    @Schema(description = "시험지 이름", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "문서 타입", example = "EXAM_SHEET", allowableValues = {"STUDENT_ANSWER_SHEET", "EXAM_SHEET", "CORRECT_ANSWER_SHEET"})
    String documentType,
    
    @Schema(description = "문서 타입 한글명", example = "문제지")
    String documentTypeName,
    
    @Schema(description = "문서 내용 (HTML)", example = "<html><body><h1>1학년 중간고사</h1></body></html>")
    String documentContent,
    
    @Schema(description = "QR 코드 URL (답안지만 해당)", example = "https://example.com/qr/123", nullable = true)
    String qrCodeUrl
) {
    public ExamDocumentDetailResponse {
        Objects.requireNonNull(documentId, "documentId는 필수입니다");
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(documentType, "documentType은 필수입니다");
        Objects.requireNonNull(documentTypeName, "documentTypeName은 필수입니다");
        Objects.requireNonNull(documentContent, "documentContent는 필수입니다");
    }
}