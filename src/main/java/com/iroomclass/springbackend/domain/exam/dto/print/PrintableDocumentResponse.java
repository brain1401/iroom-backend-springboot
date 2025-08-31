package com.iroomclass.springbackend.domain.exam.dto.print;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "인쇄 가능한 문서 목록 응답")
public record PrintableDocumentResponse(
        @Schema(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID examSheetId,

        @Schema(description = "시험명", example = "1학년 중간고사") String examName,

        @Schema(description = "인쇄 가능한 문서 목록") List<DocumentInfo> documents) {
    public PrintableDocumentResponse {
        Objects.requireNonNull(examSheetId, "examSheetId은 필수입니다");
        Objects.requireNonNull(examName, "examName는 필수입니다");
        Objects.requireNonNull(documents, "documents은 필수입니다");
    }

    @Schema(description = "문서 정보")
    public record DocumentInfo(
            @Schema(description = "문서 ID", example = "123e4567-e89b-12d3-a456-426614174001") UUID documentId,

            @Schema(description = "문서 타입", example = "EXAM_SHEET", allowableValues = {
                    "EXAM_SHEET", "CORRECT_ANSWER_SHEET", "STUDENT_ANSWER_SHEET" }) String documentType,

            @Schema(description = "문서 타입명", example = "문제지") String documentTypeName,

            @Schema(description = "문서명", example = "1학년 중간고사 문제지") String documentName,

            @Schema(description = "QR 코드 URL (답안지인 경우)", example = "https://example.com/qr/123") String qrCodeUrl,

            @Schema(description = "인쇄 가능 여부", example = "true") boolean printable){
        public DocumentInfo {
            Objects.requireNonNull(documentId, "documentId은 필수입니다");
            Objects.requireNonNull(documentType, "documentType는 필수입니다");
            Objects.requireNonNull(documentTypeName, "documentTypeName는 필수입니다");
            Objects.requireNonNull(documentName, "documentName는 필수입니다");
        }
    }
}