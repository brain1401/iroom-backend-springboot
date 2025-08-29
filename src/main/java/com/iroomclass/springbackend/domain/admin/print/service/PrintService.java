package com.iroomclass.springbackend.domain.admin.print.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDocument;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDocumentRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintRequest;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintResponse;
import com.iroomclass.springbackend.domain.admin.print.dto.PrintableDocumentResponse;
import com.iroomclass.springbackend.domain.admin.print.util.PdfGenerator;
import com.iroomclass.springbackend.domain.admin.print.util.QrCodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrintService {

    private final ExamRepository examRepository;
    private final ExamDocumentRepository examDocumentRepository;
    private final ExamSheetRepository examSheetRepository;
    private final PdfGenerator pdfGenerator;
    private final QrCodeGenerator qrCodeGenerator;

    // PDF 파일 저장소 (실제로는 Redis나 파일 시스템 사용 권장)
    private final ConcurrentHashMap<String, byte[]> pdfStorage = new ConcurrentHashMap<>();

    /**
     * 인쇄 가능한 문서 목록 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 인쇄 가능한 문서 목록
     */
    public PrintableDocumentResponse getPrintableDocuments(Long examSheetId) {
        log.info("인쇄 가능한 문서 목록 조회: examSheetId={}", examSheetId);
        
        // 1단계: 시험지에 속한 문서 목록 조회
        List<ExamDocument> documents = examDocumentRepository.findByExamSheetId(examSheetId);
        
        if (documents.isEmpty()) {
            log.warn("시험지에 문서가 없습니다: examSheetId={}", examSheetId);
            ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("시험지를 찾을 수 없습니다: " + examSheetId));
            return new PrintableDocumentResponse(
                examSheet.getId(),
                examSheet.getExamName(),
                new ArrayList<>()
            );
        }
        
        // 2단계: 시험지 정보 조회 (첫 번째 문서에서)
        ExamSheet examSheet = documents.get(0).getExamSheet();
        
        // 3단계: 문서 정보 변환
        List<PrintableDocumentResponse.DocumentInfo> documentInfos = documents.stream()
            .map(this::convertToDocumentInfo)
            .collect(Collectors.toList());
        
        log.info("인쇄 가능한 문서 목록 조회 완료: examSheetId={}, documentCount={}", 
            examSheetId, documentInfos.size());
        
        return new PrintableDocumentResponse(
            examSheet.getId(),
            examSheet.getExamName(),
            documentInfos
        );
    }

    /**
     * 문서 인쇄 요청 처리
     * 
     * @param request 인쇄 요청
     * @return 인쇄 응답
     */
    @Transactional
    public PrintResponse processPrintRequest(PrintRequest request) {
        log.info("문서 인쇄 요청 처리: examSheetId={}, documentTypes={}", 
            request.examSheetId(), request.documentTypes());
        
        // 1단계: 요청된 문서 타입들의 문서 조회
        List<ExamDocument.DocumentType> documentTypes = request.documentTypes().stream()
            .map(ExamDocument.DocumentType::valueOf)
            .collect(Collectors.toList());
        
        // 먼저 examSheetId로 모든 문서를 가져온 후 필터링
        List<ExamDocument> allDocuments = examDocumentRepository.findByExamSheetId(request.examSheetId());
        List<ExamDocument> documents = allDocuments.stream()
            .filter(doc -> documentTypes.contains(doc.getDocumentType()))
            .collect(Collectors.toList());
        
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("인쇄할 문서를 찾을 수 없습니다");
        }
        
        // 3단계: PDF 생성 (ExamDocument의 기존 내용 사용)
        List<PdfGenerator.DocumentInfo> pdfDocuments = documents.stream()
            .map(doc -> new PdfGenerator.DocumentInfo(
                cleanHtmlContent(doc.getDocumentContent()),  // HTML 내용 정리
                doc.getDocumentType().name(),
                generateDocumentName(documents.get(0).getExamSheet().getExamName(), doc.getDocumentType().name()),
                doc.getQrCodeUrl()  // QR 코드 URL 추가
            ))
            .collect(Collectors.toList());
        
        byte[] pdfContent = pdfGenerator.mergeDocumentsToPdf(pdfDocuments);
        
        // 4단계: 인쇄 작업 ID 생성
        String printJobId = generatePrintJobId();
        
        // 5단계: PDF 파일 저장
        savePdfFile(printJobId, pdfContent);
        
        // 6단계: 파일명 생성
        String fileName = generateFileName(documents.get(0).getExamSheet().getExamName(), request.fileName());
        
        // 7단계: 다운로드 URL 및 파일 크기 설정
        String downloadUrl = generateDownloadUrl(printJobId);
        Long fileSize = (long) pdfContent.length;
        
        log.info("문서 인쇄 요청 처리 완료: examSheetId={}, printJobId={}, fileName={}, fileSize={}", 
            request.examSheetId(), printJobId, fileName, pdfContent.length);
        
        return new PrintResponse(
            printJobId,
            downloadUrl,
            fileName,
            fileSize,
            "COMPLETED",
            "PDF 생성이 완료되었습니다"
        );
    }

    /**
     * PDF 파일 저장
     * 
     * @param printJobId 인쇄 작업 ID
     * @param pdfContent PDF 바이트 배열
     */
    public void savePdfFile(String printJobId, byte[] pdfContent) {
        pdfStorage.put(printJobId, pdfContent);
        log.info("PDF 파일 저장 완료: printJobId={}, fileSize={}", printJobId, pdfContent.length);
    }

    /**
     * PDF 파일 조회
     * 
     * @param printJobId 인쇄 작업 ID
     * @return PDF 바이트 배열 (없으면 null)
     */
    public byte[] getPdfFile(String printJobId) {
        byte[] pdfContent = pdfStorage.get(printJobId);
        if (pdfContent != null) {
            log.info("PDF 파일 조회 성공: printJobId={}, fileSize={}", printJobId, pdfContent.length);
        } else {
            log.warn("PDF 파일을 찾을 수 없습니다: printJobId={}", printJobId);
        }
        return pdfContent;
    }

    /**
     * PDF 파일 삭제
     * 
     * @param printJobId 인쇄 작업 ID
     */
    public void deletePdfFile(String printJobId) {
        byte[] removed = pdfStorage.remove(printJobId);
        if (removed != null) {
            log.info("PDF 파일 삭제 완료: printJobId={}", printJobId);
        } else {
            log.warn("삭제할 PDF 파일을 찾을 수 없습니다: printJobId={}", printJobId);
        }
    }

    /**
     * 문서 정보를 DTO로 변환
     */
    private PrintableDocumentResponse.DocumentInfo convertToDocumentInfo(ExamDocument document) {
        return new PrintableDocumentResponse.DocumentInfo(
            document.getId(),
            document.getDocumentType().name(),
            document.getDocumentType().name(),
            generateDocumentName(document.getExamSheet().getExamName(), document.getDocumentType().name()),
            document.getQrCodeUrl(),
            true
        );
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

    /**
     * HTML 내용을 안전하게 XHTML로 정규화합니다.
     * 
     * 핵심 원칙:
     * 1. 텍스트 내용의 <, > 만 &lt;, &gt;로 이스케이프
     * 2. HTML 태그는 그대로 유지 (escape 하지 않음)
     * 3. XHTML 형식으로 변환 (<br />, <hr />, <img ... />)
     * 
     * @param htmlContent 처리할 HTML 내용
     * @return 정리된 XHTML 내용
     */
    private String cleanHtmlContent(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        try {
            log.debug("HTML 정리 시작 - 원본 길이: {}", htmlContent.length());
            
            // 1. Jsoup을 사용하여 HTML을 XHTML로 정규화
            // - 태그는 태그로 인식하고, 텍스트 내용만 이스케이프
            Document doc = Jsoup.parseBodyFragment(htmlContent);
            
            // 2. 모든 요소에서 인라인 스타일 제거 (폰트 설정 방해 방지)
            doc.select("*[style]").removeAttr("style");
            
            // 3. 모든 텍스트 노드의 <, > 만 엔티티로 변환 (태그는 그대로)
            doc.traverse(new NodeVisitor() {
                @Override 
                public void head(Node node, int depth) {
                    if (node instanceof TextNode) {
                        TextNode textNode = (TextNode) node;
                        String text = textNode.text();
                        // 수식 기호 <, > 만 &lt;, &gt;로 변환
                        text = text.replace("<", "&lt;").replace(">", "&gt;");
                        textNode.text(text);
                    }
                }
                
                @Override 
                public void tail(Node node, int depth) {}
            });
            
            // 5. XHTML 출력 설정 (자동 자가닫힘 태그 생성)
            doc.outputSettings()
               .syntax(Document.OutputSettings.Syntax.xml)
               .escapeMode(Entities.EscapeMode.xhtml)
               .prettyPrint(false);
            
            // 6. XHTML로 변환
            String cleanedContent = doc.body().html();
            
            // 7. 추가 정리 (혹시 모를 누락된 자가닫힘 태그)
            cleanedContent = cleanedContent
                .replaceAll("<br>", "<br />")
                .replaceAll("<hr>", "<hr />")
                .replaceAll("<img([^>]*?)(?<!\\/)>", "<img$1 />");
            
            // 8. 최종 정리
            cleanedContent = cleanedContent.trim();
            
            log.debug("HTML 정리 완료 - 원본 길이: {}, 정리된 길이: {}", 
                htmlContent.length(), cleanedContent.length());
            
            return cleanedContent;
            
        } catch (Exception e) {
            log.error("HTML 정리 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 원본 내용 반환
            return htmlContent;
        }
    }
}
