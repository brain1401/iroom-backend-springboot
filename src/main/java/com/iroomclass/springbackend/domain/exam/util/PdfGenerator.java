package com.iroomclass.springbackend.domain.exam.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final LatexRenderer latexRenderer;

    /**
     * 단일 문서를 PDF로 변환
     * 
     * @param htmlContent HTML 내용
     * @param documentType 문서 타입 (EXAM_SHEET, CORRECT_ANSWER_SHEET, STUDENT_ANSWER_SHEET)
     * @return PDF 바이트 배열
     */
    public byte[] generatePdfFromHtml(String htmlContent, String documentType) {
        try {
            log.info("PDF 생성 시작: documentType={}", documentType);
            
            // HTML을 PDF용으로 향상 (QR 코드 URL은 null로 전달)
            String enhancedHtml = enhanceHtmlForPdf(htmlContent, documentType, null);
            
            // 실제 HTML → PDF 변환
            byte[] pdfContent = convertHtmlToPdf(enhancedHtml, documentType);
            
            log.info("PDF 생성 완료: documentType={}, size={} bytes", documentType, pdfContent.length);
            return pdfContent;
            
        } catch (Exception e) {
            log.error("PDF 생성 실패: documentType={}", documentType, e);
            throw new RuntimeException("PDF 생성에 실패했습니다", e);
        }
    }

    /**
     * 여러 문서를 하나의 PDF로 합치기
     */
    public byte[] mergeDocumentsToPdf(List<DocumentInfo> documents) {
        try {
            log.info("문서 합치기 시작: documentCount={}", documents.size());
            
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("합칠 문서가 없습니다");
            }
            
            // 문서가 1개인 경우: 단일 문서 PDF 생성
            if (documents.size() == 1) {
                DocumentInfo singleDoc = documents.get(0);
                log.info("단일 문서 PDF 생성: documentType={}", singleDoc.documentType);
                return generatePdfFromHtml(singleDoc.htmlContent, singleDoc.documentType);
            }
            
            // 문서가 2개 이상인 경우: 여러 문서를 하나의 HTML로 합치기
            log.info("여러 문서 합치기: documentCount={}", documents.size());
            String mergedHtml = mergeHtmlDocuments(documents);
            
            // 합쳐진 HTML을 PDF로 변환
            byte[] mergedPdf = convertHtmlToPdf(mergedHtml, "MERGED");
            
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
    private byte[] convertHtmlToPdf(String htmlContent, String documentType) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            
            // HTML 내용 검증
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                throw new IllegalArgumentException("HTML 내용이 비어있습니다.");
            }
            
            // HTML 내용 로깅 (디버깅용)
            log.debug("PDF 변환 시작 - HTML 내용 길이: {}", htmlContent.length());
            log.debug("HTML 내용 미리보기: {}", htmlContent.substring(0, Math.min(200, htmlContent.length())));
            
            // HTML 내용 설정
            builder.withHtmlContent(htmlContent, null);
            
            // 한글 폰트 임베딩 (PDF에 폰트 포함)
            try {
                // NanumGothic 폰트 등록 및 임베딩
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/NanumGothic-Regular.ttf"), "NanumGothic");
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/NanumGothic-Bold.ttf"), "NanumGothic-Bold");
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/NanumGothic-ExtraBold.ttf"), "NanumGothic-ExtraBold");
                log.debug("한글 폰트 임베딩 완료: NanumGothic, NanumGothic-Bold, NanumGothic-ExtraBold");
            } catch (Exception e) {
                log.warn("폰트 임베딩 실패: {}", e.getMessage());
            }
            
            // 출력 스트림 설정
            builder.toStream(baos);
            
            // PDF 생성
            builder.run();
            
            log.debug("PDF 변환 완료 - 크기: {} bytes", baos.size());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("HTML을 PDF로 변환하는 중 오류 발생: {}", e.getMessage());
            log.error("HTML 내용: {}", htmlContent);
            
            // HTML 파싱 에러인 경우 더 자세한 정보 제공
            if (e.getCause() instanceof org.xml.sax.SAXParseException) {
                org.xml.sax.SAXParseException saxError = (org.xml.sax.SAXParseException) e.getCause();
                log.error("HTML 파싱 에러 - 라인: {}, 컬럼: {}, 메시지: {}", 
                    saxError.getLineNumber(), saxError.getColumnNumber(), saxError.getMessage());
                
                // HTML 파싱 에러 시 기본 HTML로 대체하여 PDF 생성 시도
                log.warn("HTML 파싱 에러로 인해 기본 HTML로 대체하여 PDF 생성을 시도합니다.");
                String fallbackHtml = createFallbackHtml(htmlContent, documentType);
                return convertHtmlToPdfWithFallback(fallbackHtml, documentType);
            }
            
            throw new RuntimeException("PDF 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * HTML을 PDF용으로 향상
     */
    private String enhanceHtmlForPdf(String htmlContent, String documentType, String qrCodeUrl) {
        // LaTeX 수식 렌더링
        String renderedHtml = latexRenderer.renderLatexInHtml(htmlContent);
        
        StringBuilder enhancedHtml = new StringBuilder();
        enhancedHtml.append("<!DOCTYPE html>");
        enhancedHtml.append("<html>");
        enhancedHtml.append("<head>");
        enhancedHtml.append("<meta charset='UTF-8'/>");
        enhancedHtml.append("<title>").append(getDocumentTypeName(documentType)).append("</title>");
        enhancedHtml.append("<style>");
        enhancedHtml.append("body { font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif !important; margin: 20px; font-size: 12px; }");
        enhancedHtml.append(".header { margin-bottom: 30px; padding-bottom: 10px; }");
        enhancedHtml.append(".header-content { display: flex; align-items: center; justify-content: center; position: relative; min-height: 80px; }");
        enhancedHtml.append(".header h1 { margin: 0; color: #333; font-size: 24px; font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif !important; flex: 1; text-align: center; }");
        enhancedHtml.append(".qr-code { position: absolute; left: 0; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; }");
        enhancedHtml.append(".qr-code img { width: 60px; height: 60px; border: 1px solid #ccc; }");
        enhancedHtml.append(".qr-code p { margin: 5px 0 0 0; font-size: 10px; }");
        enhancedHtml.append(".content { line-height: 1.6; margin-top: 20px; font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif !important; }");
        enhancedHtml.append(".content * { font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif !important; }");
        enhancedHtml.append(".content p, .content div, .content span, .content strong, .content b, .content h1, .content h2, .content h3, .content h4, .content h5, .content h6 { font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif !important; }");
        enhancedHtml.append(".page-break { page-break-before: always; }");
        enhancedHtml.append("@media print { body { margin: 15px; } }");
        enhancedHtml.append("</style>");
        enhancedHtml.append("</head>");
        enhancedHtml.append("<body>");
        
        // 문서 타입별 헤더 추가
        enhancedHtml.append("<div class='header'>");
        enhancedHtml.append("<div class='header-content'>");
        
        // QR 코드 추가 (답안지인 경우)
        if ("STUDENT_ANSWER_SHEET".equals(documentType)) {
            enhancedHtml.append("<div class='qr-code'>");
            
            // DB에 저장된 QR 코드 URL이 있으면 사용, 없으면 기본값 생성
            if (qrCodeUrl != null && !qrCodeUrl.trim().isEmpty()) {
                // DB에 저장된 QR 코드 URL 사용
                enhancedHtml.append("<img src='").append(qrCodeUrl).append("' alt='QR Code'/>");
                log.debug("DB에 저장된 QR 코드 URL 사용: {}", qrCodeUrl);
            } else {
                // 기본 QR 코드 생성 (fallback)
                String fallbackQrCode = qrCodeGenerator.generateQrCodeBase64("STUDENT_ANSWER_SHEET");
                enhancedHtml.append("<img src='data:image/png;base64,")
                    .append(fallbackQrCode)
                    .append("' alt='QR Code'/>");
                log.warn("DB에 QR 코드 URL이 없어 기본 QR 코드를 생성합니다");
            }
            
            enhancedHtml.append("<p>QR 코드</p>");
            enhancedHtml.append("</div>");
        }
        
        enhancedHtml.append("<h1>").append(getDocumentTypeName(documentType)).append("</h1>");
        enhancedHtml.append("</div>");
        enhancedHtml.append("</div>");
        
        // hr선 추가
        enhancedHtml.append("<hr style='margin: 20px 0; border: 1px solid #333;'/>");
        
        enhancedHtml.append("<div class='content'>");
        enhancedHtml.append(renderedHtml); // LaTeX가 렌더링된 HTML 사용
        enhancedHtml.append("</div>");
        
        enhancedHtml.append("</body>");
        enhancedHtml.append("</html>");
        
        return enhancedHtml.toString();
    }

    /**
     * HTML을 PDF로 변환하는 메서드 (fallback 버전)
     */
    private byte[] convertHtmlToPdfWithFallback(String htmlContent, String documentType) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            
            // HTML 내용 검증
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                throw new IllegalArgumentException("HTML 내용이 비어있습니다.");
            }
            
            // HTML 내용을 PDF용으로 향상 (CSS 스타일 포함)
            String enhancedHtml = enhanceHtmlForPdf(htmlContent, documentType, null); // QR 코드 URL은 현재 사용되지 않음
            
            // HTML 내용 로깅 (디버깅용)
            log.debug("PDF 변환 시작 - HTML 내용 길이: {}", enhancedHtml.length());
            log.debug("HTML 내용 미리보기: {}", enhancedHtml.substring(0, Math.min(200, enhancedHtml.length())));
            
            // HTML 내용 설정
            builder.withHtmlContent(enhancedHtml, null);
            
            // 한글 폰트 임베딩 (PDF에 폰트 포함)
            try {
                // NanumGothic 폰트 등록 및 임베딩
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/NanumGothic-Regular.ttf"), "NanumGothic");
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/NanumGothic-Bold.ttf"), "NanumGothic-Bold");
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/NanumGothic-ExtraBold.ttf"), "NanumGothic-ExtraBold");
                log.debug("한글 폰트 임베딩 완료: NanumGothic, NanumGothic-Bold, NanumGothic-ExtraBold");
            } catch (Exception e) {
                log.warn("폰트 임베딩 실패: {}", e.getMessage());
            }
            
            // 출력 스트림 설정
            builder.toStream(baos);
            
            // PDF 생성
            builder.run();
            
            log.debug("PDF 변환 완료 - 크기: {} bytes", baos.size());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("HTML을 PDF로 변환하는 중 오류 발생 (fallback): {}", e.getMessage());
            log.error("HTML 내용: {}", htmlContent);
            
            throw new RuntimeException("PDF 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 기본 HTML 생성
     */
    private String createFallbackHtml(String originalHtml, String documentType) {
        // LaTeX 수식 렌더링
        String renderedHtml = latexRenderer.renderLatexInHtml(originalHtml);
        
        StringBuilder fallbackHtml = new StringBuilder();
        fallbackHtml.append("<!DOCTYPE html>");
        fallbackHtml.append("<html>");
        fallbackHtml.append("<head>");
        fallbackHtml.append("<meta charset='UTF-8'/>");
        fallbackHtml.append("<title>").append(getDocumentTypeName(documentType)).append("</title>");
        fallbackHtml.append("<style>");
        fallbackHtml.append("body { font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Arial', sans-serif; margin: 20px; font-size: 12px; }");
        fallbackHtml.append(".header { margin-bottom: 30px; padding-bottom: 10px; }");
        fallbackHtml.append(".header-content { display: flex; align-items: center; justify-content: center; position: relative; min-height: 80px; }");
        fallbackHtml.append(".header h1 { margin: 0; color: #333; font-size: 24px; font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Arial', sans-serif; flex: 1; text-align: center; }");
        fallbackHtml.append(".qr-code { position: absolute; left: 0; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; }");
        fallbackHtml.append(".qr-code img { width: 60px; height: 60px; border: 1px solid #ccc; }");
        fallbackHtml.append(".qr-code p { margin: 5px 0 0 0; font-size: 10px; }");
        fallbackHtml.append(".content { line-height: 1.6; margin-top: 20px; font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Arial', sans-serif; }");
        fallbackHtml.append(".page-break { page-break-before: always; }");
        fallbackHtml.append("@media print { body { margin: 15px; } }");
        fallbackHtml.append("</style>");
        fallbackHtml.append("</head>");
        fallbackHtml.append("<body>");
        
        // 문서 타입별 헤더 추가
        fallbackHtml.append("<div class='header'>");
        fallbackHtml.append("<div class='header-content'>");
        
        // QR 코드 추가 (답안지인 경우)
        if ("STUDENT_ANSWER_SHEET".equals(documentType)) {
            fallbackHtml.append("<div class='qr-code'>");
            fallbackHtml.append("<img src='data:image/png;base64,")
                .append(qrCodeGenerator.generateQrCodeBase64("STUDENT_ANSWER_SHEET"))
                .append("' alt='QR Code'/>");
            fallbackHtml.append("<p>QR 코드</p>");
            fallbackHtml.append("</div>");
        }
        
        fallbackHtml.append("<h1>").append(getDocumentTypeName(documentType)).append("</h1>");
        fallbackHtml.append("</div>");
        fallbackHtml.append("</div>");
        
        // hr선 추가
        fallbackHtml.append("<hr style='margin: 20px 0; border: 1px solid #333;'/>");
        
        fallbackHtml.append("<div class='content'>");
        fallbackHtml.append(renderedHtml); // LaTeX가 렌더링된 HTML 사용
        fallbackHtml.append("</div>");
        
        fallbackHtml.append("</body>");
        fallbackHtml.append("</html>");
        
        return fallbackHtml.toString();
    }

    /**
     * 문서 타입명 반환
     */
    private String getDocumentTypeName(String documentType) {
        switch (documentType) {
            case "EXAM_SHEET":
                return "문제지";
            case "CORRECT_ANSWER_SHEET":
                return "답안지";
            case "STUDENT_ANSWER_SHEET":
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
        public final String qrCodeUrl;

        public DocumentInfo(String htmlContent, String documentType, String documentName, String qrCodeUrl) {
            this.htmlContent = htmlContent;
            this.documentType = documentType;
            this.documentName = documentName;
            this.qrCodeUrl = qrCodeUrl;
        }
    }

    /**
     * 여러 HTML 문서를 하나로 합치기
     */
    private String mergeHtmlDocuments(List<DocumentInfo> documents) {
        StringBuilder mergedHtml = new StringBuilder();
        mergedHtml.append("<!DOCTYPE html>");
        mergedHtml.append("<html>");
        mergedHtml.append("<head>");
        mergedHtml.append("<meta charset='UTF-8'/>");
        mergedHtml.append("<title>합쳐진 문서</title>");
        mergedHtml.append("<style>");
        mergedHtml.append("body { font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif; margin: 20px; font-size: 12px; }");
        mergedHtml.append(".header { margin-bottom: 30px; padding-bottom: 10px; }");
        mergedHtml.append(".header-content { display: flex; align-items: center; justify-content: center; position: relative; min-height: 80px; }");
        mergedHtml.append(".header h1 { margin: 0; color: #333; font-size: 24px; font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif; flex: 1; text-align: center; }");
        mergedHtml.append(".qr-code { position: absolute; left: 0; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; }");
        mergedHtml.append(".qr-code img { width: 60px; height: 60px; border: 1px solid #ccc; }");
        mergedHtml.append(".qr-code p { margin: 5px 0 0 0; font-size: 10px; }");
        mergedHtml.append(".content { line-height: 1.6; margin-top: 20px; font-family: 'NanumGothic', 'NanumGothic-Bold', 'NanumGothic-ExtraBold', 'Malgun Gothic', 'Dotum', 'Gulim', 'Arial Unicode MS', 'Arial', sans-serif; }");
        mergedHtml.append(".page-break { page-break-before: always; }");
        mergedHtml.append("@media print { body { margin: 15px; } }");
        mergedHtml.append("</style>");
        mergedHtml.append("</head>");
        mergedHtml.append("<body>");
        
        for (int i = 0; i < documents.size(); i++) {
            DocumentInfo doc = documents.get(i);
            
            // 첫 번째 문서가 아닌 경우 페이지 구분자 추가
            if (i > 0) {
                mergedHtml.append("<div class='page-break'></div>");
            }
            
            // 문서 헤더
            mergedHtml.append("<div class='header'>");
            mergedHtml.append("<div class='header-content'>");
            
            // QR 코드 추가 (답안지인 경우)
            if ("STUDENT_ANSWER_SHEET".equals(doc.documentType)) {
                mergedHtml.append("<div class='qr-code'>");
                
                // DB에 저장된 QR 코드 URL이 있으면 사용, 없으면 기본값 생성
                if (doc.qrCodeUrl != null && !doc.qrCodeUrl.trim().isEmpty()) {
                    // DB에 저장된 QR 코드 URL 사용
                    mergedHtml.append("<img src='").append(doc.qrCodeUrl).append("' alt='QR Code'/>");
                    log.debug("DB에 저장된 QR 코드 URL 사용: {}", doc.qrCodeUrl);
                } else {
                    // 기본 QR 코드 생성 (fallback)
                    String fallbackQrCode = qrCodeGenerator.generateQrCodeBase64("STUDENT_ANSWER_SHEET");
                    mergedHtml.append("<img src='data:image/png;base64,")
                        .append(fallbackQrCode)
                        .append("' alt='QR Code'/>");
                    log.warn("DB에 QR 코드 URL이 없어 기본 QR 코드를 생성합니다");
                }
                
                mergedHtml.append("<p>QR 코드</p>");
                mergedHtml.append("</div>");
            }
            
            mergedHtml.append("<h1>").append(doc.documentName).append("</h1>");
            mergedHtml.append("</div>");
            mergedHtml.append("</div>");
            
            // hr선 추가
            mergedHtml.append("<hr style='margin: 20px 0; border: 1px solid #333;'/>");
            
            // 문서 내용
            mergedHtml.append("<div class='content'>");
            // LaTeX 수식 렌더링 적용
            String renderedContent = latexRenderer.renderLatexInHtml(doc.htmlContent);
            mergedHtml.append(renderedContent);
            mergedHtml.append("</div>");
        }
        
        mergedHtml.append("</body>");
        mergedHtml.append("</html>");
        
        return mergedHtml.toString();
    }
}
