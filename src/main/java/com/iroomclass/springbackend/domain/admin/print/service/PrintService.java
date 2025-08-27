package com.iroomclass.springbackend.domain.admin.print.service;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDocument;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDocumentRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDraftQuestionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrintService {

    private final ExamRepository examRepository;
    private final ExamDocumentRepository examDocumentRepository;
    private final QuestionRepository questionRepository;
    private final ExamDraftQuestionRepository examDraftQuestionRepository;
    private final PdfGenerator pdfGenerator;
    private final QrCodeGenerator qrCodeGenerator;

    // PDF 파일 저장소 (실제로는 Redis나 파일 시스템 사용 권장)
    private final ConcurrentHashMap<String, byte[]> pdfStorage = new ConcurrentHashMap<>();

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
        
        // 3단계: 시험 문제 목록 조회
        List<Question> questions = getExamQuestions(exam);
        
        // 4단계: 동적 HTML 생성 및 PDF 변환
        List<PdfGenerator.DocumentInfo> pdfDocuments = new ArrayList<>();
        
        for (ExamDocument.DocumentType documentType : documentTypes) {
            String htmlContent;
            String documentName;
            
            switch (documentType) {
                case QUESTION_PAPER:
                    htmlContent = generateQuestionPaperHtml(exam, questions);
                    documentName = exam.getExamName() + " 문제지";
                    break;
                case ANSWER_KEY:
                    htmlContent = generateAnswerKeyHtml(exam, questions);
                    documentName = exam.getExamName() + " 답안지";
                    break;
                case ANSWER_SHEET:
                    htmlContent = generateAnswerSheetHtml(exam);
                    documentName = exam.getExamName() + " 학생 답안지";
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 문서 타입입니다: " + documentType);
            }
            
            pdfDocuments.add(new PdfGenerator.DocumentInfo(
                htmlContent,
                documentType.name(),
                documentName
            ));
        }
        
        byte[] pdfContent = pdfGenerator.mergeDocumentsToPdf(pdfDocuments);
        
        // 4단계: 인쇄 작업 ID 생성
        String printJobId = generatePrintJobId();
        
        // 5단계: PDF 파일 저장
        savePdfFile(printJobId, pdfContent);
        
        // 6단계: 파일명 생성
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

    /**
     * 시험 문제 목록 조회
     */
    private List<Question> getExamQuestions(Exam exam) {
        // 1단계: 시험 문제 목록을 가져오기 위해 ExamDraftQuestionRepository를 사용
        List<Long> questionIds = examDraftQuestionRepository.findByExamDraftIdOrderBySeqNo(exam.getExamDraft().getId())
            .stream()
            .map(examDraftQuestion -> examDraftQuestion.getQuestion().getId())
            .collect(Collectors.toList());

        // 2단계: 문제 ID 목록을 사용하여 QuestionRepository에서 문제 목록을 조회
        List<Question> questions = questionRepository.findAllById(questionIds);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("시험에 포함된 문제를 찾을 수 없습니다.");
        }

        log.info("시험 문제 목록 조회 완료: examId={}, questionCount={}", exam.getId(), questions.size());
        return questions;
    }

    /**
     * 시험 문제지 HTML 생성
     */
    private String generateQuestionPaperHtml(Exam exam, List<Question> questions) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>").append(exam.getExamName()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: 'Malgun Gothic', 'Arial', sans-serif; margin: 20px; font-size: 12px; }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 10px; }");
        html.append(".header h1 { margin: 0; color: #333; font-size: 24px; }");
        html.append(".problem { margin-bottom: 30px; page-break-inside: avoid; }");
        html.append(".problem-number { font-weight: bold; font-size: 16px; color: #333; margin-bottom: 10px; }");
        html.append(".problem-content { margin: 10px 0; line-height: 1.6; }");
        html.append(".problem-image { text-align: center; margin: 15px 0; }");
        html.append(".problem-image img { max-width: 100%; height: auto; border: 1px solid #ddd; }");
        html.append(".page-break { page-break-before: always; }");
        html.append("@media print { body { margin: 15px; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // 시험명 헤더
        html.append("<div class='header'>");
        html.append("<h1>").append(exam.getExamName()).append("</h1>");
        html.append("</div>");
        
        // 문제들 동적 생성
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            html.append("<div class='problem'>");
            html.append("<div class='problem-number'>").append(i + 1).append(".</div>");
            html.append("<div class='problem-content'>").append(question.getStem()).append("</div>");
            
            // 이미지가 있는 경우 (HTML 내용에 이미지 태그가 포함되어 있을 수 있음)
            if (question.getStem().contains("<img")) {
                html.append("<div class='problem-image'>");
                html.append("<!-- 이미지는 HTML 내용에 포함되어 있음 -->");
                html.append("</div>");
            }
            html.append("</div>");
            
            // 페이지 나누기 (10문제마다)
            if ((i + 1) % 10 == 0 && i < questions.size() - 1) {
                html.append("<div class='page-break'></div>");
            }
        }
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * 시험 답안지 HTML 생성
     */
    private String generateAnswerKeyHtml(Exam exam, List<Question> questions) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>").append(exam.getExamName()).append(" 답안지</title>");
        html.append("<style>");
        html.append("body { font-family: 'Malgun Gothic', 'Arial', sans-serif; margin: 20px; font-size: 12px; }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 10px; }");
        html.append(".header h1 { margin: 0; color: #333; font-size: 24px; }");
        html.append(".answer-item { margin-bottom: 20px; padding: 10px; border-left: 3px solid #007bff; }");
        html.append(".answer-number { font-weight: bold; font-size: 14px; color: #007bff; }");
        html.append(".answer-content { margin-left: 20px; font-size: 14px; }");
        html.append(".page-break { page-break-before: always; }");
        html.append("@media print { body { margin: 15px; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // 시험명 헤더
        html.append("<div class='header'>");
        html.append("<h1>").append(exam.getExamName()).append(" 답안지</h1>");
        html.append("</div>");
        
        // 답안들 동적 생성
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            html.append("<div class='answer-item'>");
            html.append("<span class='answer-number'>").append(i + 1).append(".</span>");
            html.append("<span class='answer-content'>").append(question.getAnswerKey()).append("</span>");
            html.append("</div>");
            
            // 페이지 나누기 (15개 답안마다)
            if ((i + 1) % 15 == 0 && i < questions.size() - 1) {
                html.append("<div class='page-break'></div>");
            }
        }
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * 학생 답안지 HTML 생성
     */
    private String generateAnswerSheetHtml(Exam exam) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>").append(exam.getExamName()).append(" 학생 답안지</title>");
        html.append("<style>");
        html.append("body { font-family: 'Malgun Gothic', 'Arial', sans-serif; margin: 20px; font-size: 12px; }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 10px; }");
        html.append(".header h1 { margin: 0; color: #333; font-size: 24px; }");
        html.append(".qr-code { position: absolute; top: 20px; left: 20px; text-align: center; }");
        html.append(".qr-code img { width: 80px; height: 80px; border: 1px solid #ccc; }");
        html.append(".qr-code p { font-size: 10px; margin: 5px 0; color: #666; }");
        html.append(".answer-section { margin-bottom: 25px; }");
        html.append(".answer-label { display: inline-block; width: 60px; background: #f0f0f0; padding: 12px 8px; text-align: center; font-weight: bold; border: 1px solid #ccc; }");
        html.append(".answer-box { display: inline-block; width: 400px; height: 45px; border: 1px solid #ccc; margin-left: 10px; background: white; }");
        html.append(".student-info { margin-bottom: 30px; padding: 15px; border: 2px solid #333; background: #f9f9f9; }");
        html.append(".student-info table { width: 100%; border-collapse: collapse; }");
        html.append(".student-info td { padding: 8px; border: 1px solid #ccc; }");
        html.append(".student-info .label { background: #e9e9e9; font-weight: bold; width: 100px; }");
        html.append("@media print { body { margin: 15px; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // QR 코드 (왼쪽 상단)
        html.append("<div class='qr-code'>");
        html.append("<img src='data:image/png;base64,")
            .append(qrCodeGenerator.generateQrCodeBase64("ANSWER_SHEET_" + exam.getId()))
            .append("' alt='QR Code'>");
        html.append("<p>QR 코드를 스캔하여<br>답안지를 제출하세요</p>");
        html.append("</div>");
        
        // 시험명 헤더
        html.append("<div class='header'>");
        html.append("<h1>").append(exam.getExamName()).append("</h1>");
        html.append("</div>");
        
        // 학생 정보 입력란
        html.append("<div class='student-info'>");
        html.append("<table>");
        html.append("<tr><td class='label'>학생 이름</td><td style='width: 200px;'>&nbsp;</td><td class='label'>학년</td><td style='width: 100px;'>&nbsp;</td></tr>");
        html.append("<tr><td class='label'>전화번호</td><td style='width: 200px;'>&nbsp;</td><td class='label'>시험일</td><td style='width: 100px;'>&nbsp;</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // 답안 입력란 (문제 수만큼)
        for (int i = 1; i <= 30; i++) { // 최대 30문제
            html.append("<div class='answer-section'>");
            html.append("<span class='answer-label'>주 ").append(i).append("</span>");
            html.append("<div class='answer-box'></div>");
            html.append("</div>");
        }
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}
