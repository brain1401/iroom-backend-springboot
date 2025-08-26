package com.iroomclass.springbackend.domain.admin.print.service;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDocument;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDocumentRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintRequest;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintResponse;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintableDocumentResponse;
import com.iroomclass.springbackend.domain.admin.print.util.PdfGenerator;
import com.iroomclass.springbackend.domain.admin.print.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrintService {

    private final ExamRepository examRepository;
    private final ExamDocumentRepository examDocumentRepository;
    private final PdfGenerator pdfGenerator;
    private final QrCodeGenerator qrCodeGenerator;

    /**
     * 인쇄 가능한 문서 목록 조회
     * 
     * @param examId 시험 ID
     * @return 인쇄 가능한 문서 목록
     */
    public PrintableDocumentResponse getPrintableDocuments(Long examId) {
        log.info("인쇄 가능한 문서 목록 조회: examId={}", examId);
        
        // 1단계: 시험 정보 조회
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));
        
        // 2단계: 해당 시험의 문서 목록 조회
        List<ExamDocument> documents = examDocumentRepository.findByExamDraft(exam.getExamDraft());
        
        // 3단계: 문서 정보 변환
        List<PrintableDocumentResponse.DocumentInfo> documentInfos = documents.stream()
            .map(this::convertToDocumentInfo)
            .collect(Collectors.toList());
        
        log.info("인쇄 가능한 문서 목록 조회 완료: examId={}, documentCount={}", 
            examId, documentInfos.size());
        
        return PrintableDocumentResponse.builder()
            .examId(examId)
            .examName(exam.getExamName())
            .documents(documentInfos)
            .build();
    }

    /**
     * 문서 인쇄 요청 처리
     * 
     * @param request 인쇄 요청
     * @return 인쇄 응답
     */
    @Transactional
    public PrintResponse processPrintRequest(PrintRequest request) {
        log.info("문서 인쇄 요청 처리: examId={}, documentTypes={}", 
            request.getExamId(), request.getDocumentTypes());
        
        // 1단계: 시험 정보 조회
        Exam exam = examRepository.findById(request.getExamId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + request.getExamId()));
        
        // 2단계: 요청된 문서 타입들의 문서 조회
        List<ExamDocument.DocumentType> documentTypes = request.getDocumentTypes().stream()
            .map(ExamDocument.DocumentType::valueOf)
            .collect(Collectors.toList());
        List<ExamDocument> documents = examDocumentRepository.findByExamDraftAndDocumentTypeIn(
            exam.getExamDraft(), documentTypes);
        
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("인쇄할 문서를 찾을 수 없습니다");
        }
        
        // 3단계: PDF 생성
        List<PdfGenerator.DocumentInfo> pdfDocuments = documents.stream()
            .map(doc -> new PdfGenerator.DocumentInfo(
                doc.getDocumentContent(),
                doc.getDocumentType().name(),
                generateDocumentName(exam.getExamName(), doc.getDocumentType().name())
            ))
            .collect(Collectors.toList());
        
        byte[] pdfContent = pdfGenerator.mergeDocumentsToPdf(pdfDocuments);
        
        // 4단계: 인쇄 작업 ID 생성
        String printJobId = generatePrintJobId();
        
        // 5단계: 파일명 생성
        String fileName = generateFileName(exam.getExamName(), request.getFileName());
        
        log.info("문서 인쇄 요청 처리 완료: examId={}, printJobId={}, fileName={}, fileSize={}", 
            request.getExamId(), printJobId, fileName, pdfContent.length);
        
        return PrintResponse.builder()
            .printJobId(printJobId)
            .downloadUrl(generateDownloadUrl(printJobId))
            .fileName(fileName)
            .fileSize((long) pdfContent.length)
            .status("COMPLETED")
            .message("PDF 생성이 완료되었습니다")
            .build();
    }

    /**
     * 문서 정보를 DTO로 변환
     */
    private PrintableDocumentResponse.DocumentInfo convertToDocumentInfo(ExamDocument document) {
        return PrintableDocumentResponse.DocumentInfo.builder()
            .documentId(document.getId())
            .documentType(document.getDocumentType().name())
            .documentTypeName(getDocumentTypeName(document.getDocumentType().name()))
            .documentName(generateDocumentName("", document.getDocumentType().name()))
            .qrCodeUrl(document.getQrCodeUrl())
            .printable(true)
            .build();
    }

    /**
     * 문서 타입명 반환
     */
    private String getDocumentTypeName(String documentType) {
        switch (documentType) {
            case "QUESTION_PAPER":
                return "문제지";
            case "ANSWER_KEY":
                return "답안지";
            case "ANSWER_SHEET":
                return "학생 답안지";
            default:
                return "문서";
        }
    }

    /**
     * 문서명 생성
     */
    private String generateDocumentName(String examName, String documentType) {
        String typeName = getDocumentTypeName(documentType);
        return examName.isEmpty() ? typeName : examName + " " + typeName;
    }

    /**
     * 인쇄 작업 ID 생성
     */
    private String generatePrintJobId() {
        return "print_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 파일명 생성
     */
    private String generateFileName(String examName, String customFileName) {
        if (customFileName != null && !customFileName.trim().isEmpty()) {
            return customFileName + ".pdf";
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return examName.replaceAll("\\s+", "_") + "_" + timestamp + ".pdf";
    }

    /**
     * 다운로드 URL 생성
     */
    private String generateDownloadUrl(String printJobId) {
        return "/api/admin/print/download/" + printJobId;
    }
}
