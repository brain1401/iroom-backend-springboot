package com.iroomclass.springbackend.domain.admin.print.dto;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인쇄 요청")
public record PrintRequest(
    @NotNull(message = "시험지 ID는 필수입니다")
    @Schema(description = "시험지 ID", example = "1")
    Long examSheetId,
    
    @NotEmpty(message = "인쇄할 문서 타입을 선택해주세요")
    @Schema(description = "인쇄할 문서 타입 목록", example = "[\"ANSWER_SHEET\", \"QUESTION_PAPER\", \"ANSWER_KEY\"]")
    List<String> documentTypes,
    
    @Schema(description = "파일명 (선택사항)", example = "1학년중간고사_문제지")
    String fileName
) {
    public PrintRequest {
        Objects.requireNonNull(examSheetId, "examSheetId은 필수입니다");
        Objects.requireNonNull(documentTypes, "documentTypes은 필수입니다");
    }
}