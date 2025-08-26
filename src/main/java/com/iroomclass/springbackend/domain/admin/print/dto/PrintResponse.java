package com.iroomclass.springbackend.domain.admin.print.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "인쇄 응답")
public class PrintResponse {

    @Schema(description = "인쇄 작업 ID", example = "print_123456")
    private String printJobId;

    @Schema(description = "PDF 다운로드 URL", example = "https://example.com/download/print_123456.pdf")
    private String downloadUrl;

    @Schema(description = "파일명", example = "1학년중간고사_문제지.pdf")
    private String fileName;

    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    private Long fileSize;

    @Schema(description = "인쇄 상태", example = "COMPLETED", allowableValues = {"PROCESSING", "COMPLETED", "FAILED"})
    private String status;

    @Schema(description = "메시지", example = "PDF 생성이 완료되었습니다")
    private String message;
}
