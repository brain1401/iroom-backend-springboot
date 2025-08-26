package com.iroomclass.springbackend.domain.admin.print.controller;

import com.iroomclass.springbackend.domain.admin.print.dto.PrintRequest;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintResponse;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintableDocumentResponse;
import com.iroomclass.springbackend.domain.admin.print.service.PrintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/print")
@RequiredArgsConstructor
@Tag(name = "관리자 - 인쇄", description = "관리자 인쇄 관련 API")
public class PrintController {

    private final PrintService printService;

    @GetMapping("/exam/{examId}/documents")
    @Operation(
        summary = "인쇄 가능한 문서 목록 조회",
        description = "해당 시험의 인쇄 가능한 문서 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "문서 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PrintableDocumentResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험")
    })
    public ResponseEntity<PrintableDocumentResponse> getPrintableDocuments(
            @Parameter(description = "시험 ID", example = "1") 
            @PathVariable Long examId) {
        
        log.info("인쇄 가능한 문서 목록 조회 요청: examId={}", examId);
        
        PrintableDocumentResponse response = printService.getPrintableDocuments(examId);
        log.info("인쇄 가능한 문서 목록 조회 성공: examId={}, documentCount={}", 
            examId, response.getDocuments().size());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exam/print")
    @Operation(
        summary = "문서 인쇄 요청",
        description = "선택된 문서들을 PDF로 생성하여 인쇄합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "인쇄 요청 성공",
            content = @Content(schema = @Schema(implementation = PrintResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 시험 또는 문서")
    })
    public ResponseEntity<PrintResponse> printDocuments(
            @Valid @RequestBody PrintRequest request) {
        
        log.info("문서 인쇄 요청: examId={}, documentTypes={}", 
            request.getExamId(), request.getDocumentTypes());
        
        PrintResponse response = printService.processPrintRequest(request);
        log.info("문서 인쇄 요청 성공: examId={}, printJobId={}, fileName={}", 
            request.getExamId(), response.getPrintJobId(), response.getFileName());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{printJobId}")
    @Operation(
        summary = "PDF 다운로드",
        description = "생성된 PDF 파일을 다운로드합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF 다운로드 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 인쇄 작업")
    })
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "인쇄 작업 ID", example = "print_123456") 
            @PathVariable String printJobId) {
        
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
