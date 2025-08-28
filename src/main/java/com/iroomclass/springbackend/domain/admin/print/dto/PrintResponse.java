package com.iroomclass.springbackend.domain.admin.print.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "인쇄 응답")
public record PrintResponse(
    @Schema(description = "인쇄 작업 ID", example = "print_123456")
    String printJobId,
    
    @Schema(description = "PDF 다운로드 URL", example = "https://example.com/download/print_123456.pdf")
    String downloadUrl,
    
    @Schema(description = "파일명", example = "1학년중간고사_문제지.pdf")
    String fileName,
    
    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    Long fileSize,
    
    @Schema(description = "인쇄 상태", example = "COMPLETED", allowableValues = {"PROCESSING", "COMPLETED", "FAILED"})
    String status,
    
    @Schema(description = "메시지", example = "PDF 생성이 완료되었습니다")
    String message
) {
    public PrintResponse {
        Objects.requireNonNull(printJobId, "printJobId은 필수입니다");
        Objects.requireNonNull(downloadUrl, "downloadUrl은 필수입니다");
        Objects.requireNonNull(fileName, "fileName는 필수입니다");
        Objects.requireNonNull(fileSize, "fileSize는 필수입니다");
        Objects.requireNonNull(status, "status은 필수입니다");
        Objects.requireNonNull(message, "message는 필수입니다");
    }
}