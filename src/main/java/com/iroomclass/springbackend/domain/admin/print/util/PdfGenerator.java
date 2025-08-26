package com.iroomclass.springbackend.domain.admin.print.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * PDF 생성 유틸리티
 * 
 * HTML 문서를 PDF로 변환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfGenerator {

    private final QrCodeGenerator qrCodeGenerator;

    /**
     * 단일 문서를 PDF로 변환
     * 
     * @param htmlContent HTML 내용
     * @param documentType 문서 타입 (QUESTION_PAPER, ANSWER_KEY, ANSWER_SHEET)
     * @return PDF 바이트 배열
     */
    public byte[] generatePdfFromHtml(String htmlContent, String documentType) {
        try {
            log.info("PDF 생성 시작: documentType={}", documentType);
            
            // HTML을 PDF용으로 향상
            String enhancedHtml = enhanceHtmlForPdf(htmlContent, documentType);
            
            // 실제 HTML → PDF 변환
            byte[] pdfContent = convertHtmlToPdf(enhancedHtml);
            
            log.info("PDF 생성 완료: documentType={}, size={} bytes", documentType, pdfContent.length);
            return pdfContent;
            
        } catch (Exception e) {
            log.error("PDF 생성 실패: documentType={}", documentType, e);
            throw new RuntimeException("PDF 생성에 실패했습니다", e);
        }
    }

    /**
     * 여러 문서를 하나의 PDF로 합치기
     * 
     * @param documents 문서 목록 (HTML 내용과 타입)
     * @return 합쳐진 PDF 바이트 배열
     */
    public byte[] mergeDocumentsToPdf(List<DocumentInfo> documents) {
        try {
            log.info("문서 합치기 시작: documentCount={}", documents.size());
            
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("합칠 문서가 없습니다");
            }
            
            // 현재는 첫 번째 문서만 반환 (나중에 PDF 합치기 기능 추가)
            DocumentInfo firstDoc = documents.get(0);
            byte[] mergedPdf = generatePdfFromHtml(firstDoc.htmlContent, firstDoc.documentType);
            
            log.info("문서 합치기 완료: documentCount={}, size={} bytes", 
                documents.size(), mergedPdf.length);
            return mergedPdf;
            
        } catch (Exception e) {
            log.error("문서 합치기 실패: documentCount={}", documents.size(), e);
            throw new RuntimeException("문서 합치기에 실패했습니다", e);
        }
    }

    /**
     * HTML을 PDF로 변환
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            
            // HTML 내용 설정
            builder.withHtmlContent(htmlContent, null);
            
            // 출력 스트림 설정
            builder.toStream(baos);
            
            // PDF 생성
            builder.run();
            
            return baos.toByteArray();
        }
    }

    /**
     * HTML을 PDF용으로 향상
     */
    private String enhanceHtmlForPdf(String htmlContent, String documentType) {
        StringBuilder enhancedHtml = new StringBuilder();
        enhancedHtml.append("<!DOCTYPE html>");
        enhancedHtml.append("<html>");
        enhancedHtml.append("<head>");
        enhancedHtml.append("<meta charset='UTF-8'>");
        enhancedHtml.append("<title>").append(getDocumentTypeName(documentType)).append("</title>");
        enhancedHtml.append("<style>");
        enhancedHtml.append("body { font-family: 'Malgun Gothic', 'Arial', sans-serif; margin: 20px; font-size: 12px; }");
        enhancedHtml.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 10px; }");
        enhancedHtml.append(".header h1 { margin: 0; color: #333; font-size: 24px; }");
        enhancedHtml.append(".content { line-height: 1.6; margin-top: 20px; }");
        enhancedHtml.append(".qr-code { text-align: center; margin: 20px 0; padding: 10px; border: 1px solid #ddd; }");
        enhancedHtml.append(".qr-code img { border: 1px solid #ccc; }");
        enhancedHtml.append(".page-break { page-break-before: always; }");
        enhancedHtml.append("@media print { body { margin: 15px; } }");
        enhancedHtml.append("</style>");
        enhancedHtml.append("</head>");
        enhancedHtml.append("<body>");
        
        // 문서 타입별 헤더 추가
        enhancedHtml.append("<div class='header'>");
        enhancedHtml.append("<h1>").append(getDocumentTypeName(documentType)).append("</h1>");
        enhancedHtml.append("</div>");
        
        // QR 코드 추가 (답안지인 경우)
        if ("ANSWER_SHEET".equals(documentType)) {
            enhancedHtml.append("<div class='qr-code'>");
            enhancedHtml.append("<p><strong>QR 코드를 스캔하여 답안지를 제출하세요</strong></p>");
            enhancedHtml.append("<img src='data:image/png;base64,")
                .append(qrCodeGenerator.generateQrCodeBase64("ANSWER_SHEET"))
                .append("' alt='QR Code' style='width: 100px; height: 100px;'>");
            enhancedHtml.append("</div>");
        }
        
        enhancedHtml.append("<div class='content'>");
        enhancedHtml.append(htmlContent);
        enhancedHtml.append("</div>");
        enhancedHtml.append("</body>");
        enhancedHtml.append("</html>");
        
        return enhancedHtml.toString();
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
     * 문서 정보 클래스
     */
    public static class DocumentInfo {
        public final String htmlContent;
        public final String documentType;
        public final String documentName;

        public DocumentInfo(String htmlContent, String documentType, String documentName) {
            this.htmlContent = htmlContent;
            this.documentType = documentType;
            this.documentName = documentName;
        }
    }
}
