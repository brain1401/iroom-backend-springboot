package com.iroomclass.springbackend.domain.exam.controller;

import java.util.UUID;

import com.iroomclass.springbackend.domain.exam.dto.print.PrintRequest;
import com.iroomclass.springbackend.domain.exam.dto.print.PrintResponse;
import com.iroomclass.springbackend.domain.exam.dto.print.PrintableDocumentResponse;
import com.iroomclass.springbackend.domain.exam.service.PrintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.iroomclass.springbackend.common.ApiResponse;
import com.iroomclass.springbackend.common.ResultStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/print")
@RequiredArgsConstructor
@Tag(name = "인쇄", description = "관리자 인쇄 관련 API")
public class PrintController {

    private final PrintService printService;

    @GetMapping("/exam/{examSheetId}/documents")
    @Operation(summary = "인쇄 가능한 문서 목록 조회", description = "해당 시험지의 인쇄 가능한 문서 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ 다운로드 URL 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.SuccessResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ 리소스 없음 - 존재하지 않는 인쇄 작업 또는 PDF 파일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "❌ 서버 오류 - S3 프리사인드 URL 생성 실패")
    })
    public ApiResponse<String> getDownloadUrl(
            @Parameter(description = "인쇄 작업 ID", example = "print_123456") @PathVariable String printJobId) {

        log.info("PDF 다운로드 URL 조회 요청: printJobId={}", printJobId);

        try {
            // PDF 파일 존재 여부 먼저 확인
            boolean pdfExists = printService.getPdfFile(printJobId);
            if (!pdfExists) {
                log.warn("PDF 파일을 S3에서 찾을 수 없습니다: printJobId={}", printJobId);
                return new ApiResponse<>(ResultStatus.ERROR, "PDF 파일을 찾을 수 없습니다", null);
            }

            // S3 프리사인드 다운로드 URL 생성
            String presignedUrl = printService.generatePresignedDownloadUrl(printJobId);

            log.info("PDF 다운로드 URL 조회 성공: printJobId={}", printJobId);

            return ApiResponse.success("다운로드 URL 조회 성공", presignedUrl);

        } catch (RuntimeException e) {
            log.error("PDF 다운로드 URL 조회 중 오류 발생: printJobId={}, error={}", printJobId, e.getMessage(), e);
            return new ApiResponse<>(ResultStatus.ERROR, "다운로드 URL 생성에 실패했습니다: " + e.getMessage(), null);
        }
    }
}
