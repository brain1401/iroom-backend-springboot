package com.iroomclass.springbackend.domain.admin.exam.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDocumentCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDocumentCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDocumentDetailResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDocumentListResponse;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDocument;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDocumentRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.admin.print.service.QrCodeGenerationService;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험지 문서 관리 서비스
 * 
 * 시험지 문서 생성, 조회, 다운로드 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamDocumentService {

    private final ExamSheetRepository examSheetRepository;
    private final ExamDocumentRepository examDocumentRepository;
    private final ExamSheetQuestionRepository examSheetQuestionRepository;
    private final QrCodeGenerationService qrCodeGenerationService;
    private final ExamRepository examRepository;

    /**
     * 시험지 문서 생성
     * 
     * @param request 시험지 문서 생성 요청
     * @return 생성된 시험지 문서 정보
     */
    @Transactional
    public ExamDocumentCreateResponse createExamDocuments(ExamDocumentCreateRequest request) {
        log.info("시험지 문서 생성 요청: 시험지 ID={}", request.examSheetId());

        // 1단계: 시험지 조회
        ExamSheet examSheet = examSheetRepository.findById(request.examSheetId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + request.examSheetId()));

        // 2단계: 기존 문서 삭제 (재생성 시)
        List<ExamDocument> existingDocuments = examDocumentRepository.findByExamSheetId(request.examSheetId());
        if (!existingDocuments.isEmpty()) {
            examDocumentRepository.deleteAll(existingDocuments);
            log.info("기존 시험지 문서 {}개 삭제", existingDocuments.size());
        }

        // 3단계: 시험지의 문제들 조회
        List<ExamSheetQuestion> examSheetQuestions = examSheetQuestionRepository
                .findByExamSheetIdOrderBySeqNo(request.examSheetId());

        if (examSheetQuestions.isEmpty()) {
            throw new IllegalArgumentException("시험지에 문제가 없습니다.");
        }

        // 4단계: 문서 생성
        List<ExamDocument> documents = new ArrayList<>();

        // 4-1. 답안지 생성
        ExamDocument answerSheet = createAnswerSheet(examSheet, examSheetQuestions);
        documents.add(answerSheet);

        // 4-2. 문제지 생성
        ExamDocument questionPaper = createQuestionPaper(examSheet, examSheetQuestions);
        documents.add(questionPaper);

        // 4-3. 답안 생성
        ExamDocument answerKey = createAnswerKey(examSheet, examSheetQuestions);
        documents.add(answerKey);

        // 5단계: 문서 저장
        examDocumentRepository.saveAll(documents);

        log.info("시험지 문서 생성 완료: 답안지, 문제지, 답안 생성됨");

        return new ExamDocumentCreateResponse(
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                examSheet.getTotalQuestions(),
                documents.size());
    }

    /**
     * 시험지별 문서 목록 조회
     * 
     * @param examSheetId 시험지 ID
     * @return 해당 시험지의 문서 목록
     */
    public ExamDocumentListResponse getExamDocumentsBySheet(UUID examSheetId) {
        log.info("시험지 {} 문서 목록 조회 요청", examSheetId);

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 문서 목록 조회
        List<ExamDocument> documents = examDocumentRepository.findByExamSheetId(examSheetId);

        List<ExamDocumentListResponse.DocumentInfo> documentInfos = new ArrayList<>();
        for (ExamDocument document : documents) {
            ExamDocumentListResponse.DocumentInfo documentInfo = new ExamDocumentListResponse.DocumentInfo(
                    document.getId(),
                    document.getDocumentType().name(),
                    document.getDocumentType().name(),
                    document.getQrCodeUrl());
            documentInfos.add(documentInfo);
        }

        log.info("시험지 {} 문서 목록 조회 완료: {}개", examSheetId, documentInfos.size());

        return new ExamDocumentListResponse(
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                documentInfos,
                documentInfos.size());
    }

    /**
     * 시험지 문서 상세 조회
     * 
     * @param documentId 문서 ID
     * @return 시험지 문서 상세 정보
     */
    public ExamDocumentDetailResponse getExamDocumentDetail(UUID documentId) {
        log.info("시험지 문서 {} 상세 조회 요청", documentId);

        ExamDocument document = examDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 문서입니다: " + documentId));

        ExamSheet examSheet = document.getExamSheet();

        log.info("시험지 문서 {} 상세 조회 완료: 타입={}", documentId, document.getDocumentType());

        return new ExamDocumentDetailResponse(
                document.getId(),
                examSheet.getId(),
                examSheet.getExamName(),
                examSheet.getGrade(),
                document.getDocumentType().name(),
                document.getDocumentType().name(),
                document.getDocumentContent(),
                document.getQrCodeUrl());
    }

    /**
     * 시험지 문서 삭제 (시험지 목록에서 삭제)
     * 
     * @param examSheetId 시험지 ID
     */
    @Transactional
    public void deleteExamDocuments(UUID examSheetId) {
        log.info("시험지 문서 삭제 요청: 시험지 ID={}", examSheetId);

        // 1단계: 시험지 존재 확인
        ExamSheet examSheet = examSheetRepository.findById(examSheetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + examSheetId));

        // 2단계: 연관된 모든 문서 삭제
        List<ExamDocument> documents = examDocumentRepository.findByExamSheetId(examSheetId);

        if (!documents.isEmpty()) {
            examDocumentRepository.deleteAll(documents);
            log.info("시험지 문서 {}개 삭제 완료: 시험지 ID={}", documents.size(), examSheetId);
        } else {
            log.info("삭제할 시험지 문서가 없습니다: 시험지 ID={}", examSheetId);
        }

        // 3단계: 시험지와 관련된 모든 데이터 삭제 (CASCADE로 자동 삭제됨)
        examSheetRepository.delete(examSheet);
        log.info("시험지 및 관련 데이터 삭제 완료: 시험지 ID={}", examSheetId);
    }

    /**
     * 답안지 생성
     */
    private ExamDocument createAnswerSheet(ExamSheet examSheet, List<ExamSheetQuestion> questions) {
        StringBuilder content = new StringBuilder();

        // 답안지 시작
        content.append(
                "<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>");

        // 답안 작성란
        content.append("<div style='margin-top: 30px;'>");

        for (ExamSheetQuestion question : questions) {
            content.append(
                    "<div style='margin: 15px 0; border: 1px solid #000; display: flex; align-items: stretch;'>");

            // 왼쪽 번호 박스 (회색)
            content.append(
                    "<div style='background-color: #f0f0f0; padding: 15px; border-right: 1px solid #000; display: flex; align-items: center; justify-content: center; min-width: 80px;'>");
            content.append("<div style='text-align: center; font-weight: bold; font-size: 14px;'>");
            content.append("주 ").append(question.getSeqNo());
            content.append("</div>");
            content.append("</div>");

            // 오른쪽 답안 작성 영역 (흰색)
            content.append("<div style='flex: 1; padding: 15px; background-color: white;'>");
            content.append(
                    "<div style='min-height: 50px; border: 1px solid #ddd; padding: 10px; background-color: #f9f9f9;'>");
            content.append("");
            content.append("</div>");
            content.append("</div>");

            content.append("</div>");
        }

        content.append("</div>");
        content.append("</div>");

        // 답안지용 QR 코드 생성
        String qrCodeUrl = qrCodeGenerationService.generateAnswerSheetQrCodeUrl(examSheet.getId());
        log.info("답안지 QR 코드 생성: examSheetId={}, qrCodeUrl={}", examSheet.getId(), qrCodeUrl);

        return ExamDocument.builder()
                .examSheet(examSheet)
                .documentType(ExamDocument.DocumentType.ANSWER_SHEET)
                .documentContent(content.toString())
                .qrCodeUrl(qrCodeUrl) // 새로 생성한 QR 코드 URL 사용
                .build();
    }

    /**
     * 문제지 생성
     */
    private ExamDocument createQuestionPaper(ExamSheet examSheet, List<ExamSheetQuestion> questions) {
        StringBuilder content = new StringBuilder();

        // 문제지 시작
        content.append(
                "<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>");

        // 문제들
        content.append("<div style='margin-top: 30px;'>");

        for (ExamSheetQuestion question : questions) {
            content.append(
                    "<div style='margin: 25px 0; border: 2px solid #000; display: flex; align-items: stretch;'>");

            // 왼쪽 번호 박스 (회색)
            content.append(
                    "<div style='background-color: #f0f0f0; padding: 20px; border-right: 2px solid #000; display: flex; align-items: center; justify-content: center; min-width: 100px;'>");
            content.append("<div style='text-align: center; font-weight: bold; font-size: 16px;'>");
            content.append("주 ").append(question.getSeqNo());
            content.append("</div>");
            content.append("</div>");

            // 오른쪽 문제 내용 영역 (흰색)
            content.append("<div style='flex: 1; padding: 20px; background-color: white;'>");
            content.append("<div style='line-height: 1.6;'>");

            // 문제 텍스트를 안전하게 HTML로 변환
            try {
                String questionHtml = question.getQuestion().getQuestionTextAsHtml();
                content.append(questionHtml);
            } catch (Exception e) {
                log.warn("문제 텍스트 HTML 변환 실패, 원본 텍스트 사용: questionId={}, error={}",
                        question.getQuestion().getId(), e.getMessage());
                // HTML 변환 실패 시 원본 텍스트를 그대로 사용
                content.append("<p>").append(question.getQuestion().getQuestionText()).append("</p>");
            }

            content.append("</div>");
            content.append("</div>");

            content.append("</div>");
        }

        content.append("</div>");
        content.append("</div>");

        return ExamDocument.builder()
                .examSheet(examSheet)
                .documentType(ExamDocument.DocumentType.QUESTION_PAPER)
                .documentContent(content.toString())
                .build();
    }

    /**
     * 답안 생성
     */
    private ExamDocument createAnswerKey(ExamSheet examSheet, List<ExamSheetQuestion> questions) {
        StringBuilder content = new StringBuilder();

        // 답안 시작
        content.append(
                "<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>");

        // 답안들
        content.append("<div style='margin-top: 30px;'>");

        for (ExamSheetQuestion question : questions) {
            content.append(
                    "<div style='margin: 25px 0; border: 2px solid #000; display: flex; align-items: stretch;'>");

            // 왼쪽 번호 박스 (회색)
            content.append(
                    "<div style='background-color: #f0f0f0; padding: 20px; border-right: 2px solid #000; display: flex; align-items: center; justify-content: center; min-width: 100px;'>");
            content.append("<div style='text-align: center; font-weight: bold; font-size: 16px;'>");
            content.append("주 ").append(question.getSeqNo());
            content.append("</div>");
            content.append("</div>");

            // 오른쪽 정답 영역 (흰색)
            content.append("<div style='flex: 1; padding: 20px; background-color: white;'>");
            content.append("<div style='line-height: 1.6;'>");
            content.append("<p>").append(question.getQuestion().getAnswerText()).append("</p>");
            content.append("</div>");
            content.append("</div>");

            content.append("</div>");
        }

        content.append("</div>");
        content.append("</div>");

        return ExamDocument.builder()
                .examSheet(examSheet)
                .documentType(ExamDocument.DocumentType.ANSWER_KEY)
                .documentContent(content.toString())
                .build();
    }

    /**
     * QR 코드 URL 생성
     */
    private String generateQrCodeUrl(UUID examSheetId) {
        // 실제로는 QR 코드 이미지를 생성하고 저장해야 하지만,
        // 여기서는 임시 URL을 반환합니다.
        return "https://example.com/qr/" + examSheetId + "/" + UUID.randomUUID().toString();
    }

    /**
     * 문서 타입 한글명 반환
     */
    private String getDocumentTypeName(ExamDocument.DocumentType documentType) {
        switch (documentType) {
            case ANSWER_SHEET:
                return "답안지";
            case QUESTION_PAPER:
                return "문제지";
            case ANSWER_KEY:
                return "답안";
            default:
                return "알 수 없음";
        }
    }
}
