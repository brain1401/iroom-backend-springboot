package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 문서 목록 조회 응답 DTO
 * 
 * 시험지별 문서 목록 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 문서 목록 조회 응답")
public record ExamDocumentListResponse(
    @Schema(description = "시험지 ID", example = "1")
    Long examSheetId,
    
    @Schema(description = "시험지 이름", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "문서 목록")
    List<DocumentInfo> documents,
    
    @Schema(description = "총 개수", example = "3")
    int totalCount
) {
    public ExamDocumentListResponse {
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(documents, "documents는 필수입니다");
    }
    
    /**
     * 문서 정보
     */
    @Schema(description = "문서 정보")
    public record DocumentInfo(
        @Schema(description = "문서 ID", example = "1")
        Long documentId,
        
        @Schema(description = "문서 타입", example = "QUESTION_PAPER", allowableValues = {"ANSWER_SHEET", "QUESTION_PAPER", "ANSWER_KEY"})
        String documentType,
        
        @Schema(description = "문서 타입 한글명", example = "문제지")
        String documentTypeName,
        
        @Schema(description = "QR 코드 URL (답안지만 해당)", example = "https://example.com/qr/123", nullable = true)
        String qrCodeUrl
    ) {
        public DocumentInfo {
            Objects.requireNonNull(documentId, "documentId는 필수입니다");
            Objects.requireNonNull(documentType, "documentType은 필수입니다");
            Objects.requireNonNull(documentTypeName, "documentTypeName은 필수입니다");
        }
    }
}