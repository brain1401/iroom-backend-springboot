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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ 문서 목록 조회 성공", content = @Content(schema = @Schema(implementation = PrintableDocumentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ 리소스 없음 - 존재하지 않는 시험지")
    })
    public ApiResponse<PrintableDocumentResponse> getPrintableDocuments(
            @Parameter(description = "시험지 ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID examSheetId) {

        log.info("인쇄 가능한 문서 목록 조회 요청: examSheetId={}", examSheetId);

        PrintableDocumentResponse response = printService.getPrintableDocuments(examSheetId);
        log.info("인쇄 가능한 문서 목록 조회 성공: examSheetId={}, documentCount={}",
                examSheetId, response.documents().size());

        return ApiResponse.success("인쇄 가능한 문서 목록 조회 성공", response);
    }

    @PostMapping("/exam/print")
    @Operation(summary = "문서 인쇄 요청", description = "선택된 문서들을 PDF로 생성하여 인쇄합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ 인쇄 요청 성공 - PDF 생성 완료", content = @Content(schema = @Schema(implementation = PrintResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ 잘못된 요청 - 문서 타입이 올바르지 않거나 필수 파라미터 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ 리소스 없음 - 존재하지 않는 시험이거나 시험에 포함된 문제가 없음")
    })
    public ApiResponse<PrintResponse> printDocuments(
            @Valid @RequestBody PrintRequest request) {

        log.info("문서 인쇄 요청: examSheetId={}, documentTypes={}",
                request.examSheetId(), request.documentTypes());

        PrintResponse response = printService.processPrintRequest(request);
        log.info("문서 인쇄 요청 성공: examSheetId={}, printJobId={}, fileName={}",
                request.examSheetId(), response.printJobId(), response.fileName());

        return ApiResponse.success("문서 인쇄 요청 성공", response);
    }

    @GetMapping("/download/{printJobId}")
    @Operation(summary = "PDF 다운로드", description = "생성된 PDF 파일을 다운로드합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ PDF 다운로드 성공 - 파일 다운로드 시작"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ 리소스 없음 - 존재하지 않는 인쇄 작업 또는 PDF 파일")
    })
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "인쇄 작업 ID", example = "print_123456") @PathVariable String printJobId) {

        log.info("PDF 다운로드 요청: printJobId={}", printJobId);

        // 실제 PDF 파일 조회
        byte[] pdfContent = printService.getPdfFile(printJobId);

        if (pdfContent == null) {
            log.warn("PDF 파일을 찾을 수 없습니다: printJobId={}", printJobId);
            return ResponseEntity.notFound().build();
        }

        log.info("PDF 다운로드 성공: printJobId={}, fileSize={}", printJobId, pdfContent.length);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + printJobId + ".pdf\"")
                .header("Content-Type", "application/pdf")
                .header("Content-Length", String.valueOf(pdfContent.length))
                .body(pdfContent);
    }
}
