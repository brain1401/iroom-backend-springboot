package com.iroomclass.springbackend.domain.admin.print.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "인쇄 가능한 문서 목록 응답")
public class PrintableDocumentResponse {

    @Schema(description = "시험 ID", example = "1")
    private Long examId;

    @Schema(description = "시험명", example = "1학년 중간고사")
    private String examName;

    @Schema(description = "인쇄 가능한 문서 목록")
    private List<DocumentInfo> documents;

    @Getter
    @Setter
    @Builder
    @Schema(description = "문서 정보")
    public static class DocumentInfo {
        @Schema(description = "문서 ID", example = "1")
        private Long documentId;

        @Schema(description = "문서 타입", example = "QUESTION_PAPER", allowableValues = {"QUESTION_PAPER", "ANSWER_KEY", "ANSWER_SHEET"})
        private String documentType;

        @Schema(description = "문서 타입명", example = "문제지")
        private String documentTypeName;

        @Schema(description = "문서명", example = "1학년 중간고사 문제지")
        private String documentName;

        @Schema(description = "QR 코드 URL (답안지인 경우)", example = "https://example.com/qr/123")
        private String qrCodeUrl;

        @Schema(description = "인쇄 가능 여부", example = "true")
        private boolean printable;
    }
}
