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
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDraft;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDraftQuestion;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDocumentRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDraftQuestionRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDraftRepository;

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
    
    private final ExamDraftRepository examDraftRepository;
    private final ExamDocumentRepository examDocumentRepository;
    private final ExamDraftQuestionRepository examDraftQuestionRepository;
    
    /**
     * 시험지 문서 생성
     * 
     * @param request 시험지 문서 생성 요청
     * @return 생성된 시험지 문서 정보
     */
    @Transactional
    public ExamDocumentCreateResponse createExamDocuments(ExamDocumentCreateRequest request) {
        log.info("시험지 문서 생성 요청: 시험지 초안 ID={}", request.getExamDraftId());
        
        // 1단계: 시험지 초안 조회
        ExamDraft examDraft = examDraftRepository.findById(request.getExamDraftId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 초안입니다: " + request.getExamDraftId()));
        
        // 2단계: 기존 문서 삭제 (재생성 시)
        List<ExamDocument> existingDocuments = examDocumentRepository.findByExamDraftId(request.getExamDraftId());
        if (!existingDocuments.isEmpty()) {
            examDocumentRepository.deleteAll(existingDocuments);
            log.info("기존 시험지 문서 {}개 삭제", existingDocuments.size());
        }
        
        // 3단계: 시험지 초안의 문제들 조회
        List<ExamDraftQuestion> examDraftQuestions = examDraftQuestionRepository.findByExamDraftIdOrderBySeqNo(request.getExamDraftId());
        
        if (examDraftQuestions.isEmpty()) {
            throw new IllegalArgumentException("시험지 초안에 문제가 없습니다.");
        }
        
        // 4단계: 문서 생성
        List<ExamDocument> documents = new ArrayList<>();
        
        // 4-1. 답안지 생성
        ExamDocument answerSheet = createAnswerSheet(examDraft, examDraftQuestions);
        documents.add(answerSheet);
        
        // 4-2. 문제지 생성
        ExamDocument questionPaper = createQuestionPaper(examDraft, examDraftQuestions);
        documents.add(questionPaper);
        
        // 4-3. 답안 생성
        ExamDocument answerKey = createAnswerKey(examDraft, examDraftQuestions);
        documents.add(answerKey);
        
        // 5단계: 문서 저장
        examDocumentRepository.saveAll(documents);
        
        log.info("시험지 문서 생성 완료: 답안지, 문제지, 답안 생성됨");
        
        return ExamDocumentCreateResponse.builder()
            .examDraftId(examDraft.getId())
            .examName(examDraft.getExamName())
            .grade(examDraft.getGrade())
            .totalQuestions(examDraft.getTotalQuestions())
            .documentCount(documents.size())
            .build();
    }
    
    /**
     * 시험지 초안별 문서 목록 조회
     * 
     * @param examDraftId 시험지 초안 ID
     * @return 해당 시험지 초안의 문서 목록
     */
    public ExamDocumentListResponse getExamDocumentsByDraft(Long examDraftId) {
        log.info("시험지 초안 {} 문서 목록 조회 요청", examDraftId);
        
        // 1단계: 시험지 초안 존재 확인
        ExamDraft examDraft = examDraftRepository.findById(examDraftId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 초안입니다: " + examDraftId));
        
        // 2단계: 문서 목록 조회
        List<ExamDocument> documents = examDocumentRepository.findByExamDraftId(examDraftId);
        
        List<ExamDocumentListResponse.DocumentInfo> documentInfos = new ArrayList<>();
        for (ExamDocument document : documents) {
            ExamDocumentListResponse.DocumentInfo documentInfo = ExamDocumentListResponse.DocumentInfo.builder()
                .documentId(document.getId())
                .documentType(document.getDocumentType().name())
                .documentTypeName(getDocumentTypeName(document.getDocumentType()))
                .qrCodeUrl(document.getQrCodeUrl())
                .build();
            documentInfos.add(documentInfo);
        }
        
        log.info("시험지 초안 {} 문서 목록 조회 완료: {}개", examDraftId, documentInfos.size());
        
        return ExamDocumentListResponse.builder()
            .examDraftId(examDraft.getId())
            .examName(examDraft.getExamName())
            .grade(examDraft.getGrade())
            .documents(documentInfos)
            .totalCount(documentInfos.size())
            .build();
    }
    
    /**
     * 시험지 문서 상세 조회
     * 
     * @param documentId 문서 ID
     * @return 시험지 문서 상세 정보
     */
    public ExamDocumentDetailResponse getExamDocumentDetail(Long documentId) {
        log.info("시험지 문서 {} 상세 조회 요청", documentId);
        
        ExamDocument document = examDocumentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 문서입니다: " + documentId));
        
        ExamDraft examDraft = document.getExamDraft();
        
        log.info("시험지 문서 {} 상세 조회 완료: 타입={}", documentId, document.getDocumentType());
        
        return ExamDocumentDetailResponse.builder()
            .documentId(document.getId())
            .examDraftId(examDraft.getId())
            .examName(examDraft.getExamName())
            .grade(examDraft.getGrade())
            .documentType(document.getDocumentType().name())
            .documentTypeName(getDocumentTypeName(document.getDocumentType()))
            .documentContent(document.getDocumentContent())
            .qrCodeUrl(document.getQrCodeUrl())
            .build();
    }
    
    /**
     * 시험지 문서 삭제 (시험지 목록에서 삭제)
     * 
     * @param examDraftId 시험지 초안 ID
     */
    @Transactional
    public void deleteExamDocuments(Long examDraftId) {
        log.info("시험지 문서 삭제 요청: 시험지 초안 ID={}", examDraftId);
        
        // 1단계: 시험지 초안 존재 확인
        ExamDraft examDraft = examDraftRepository.findById(examDraftId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 초안입니다: " + examDraftId));
        
        // 2단계: 연관된 모든 문서 삭제
        List<ExamDocument> documents = examDocumentRepository.findByExamDraftId(examDraftId);
        
        if (!documents.isEmpty()) {
            examDocumentRepository.deleteAll(documents);
            log.info("시험지 문서 {}개 삭제 완료: 시험지 초안 ID={}", documents.size(), examDraftId);
        } else {
            log.info("삭제할 시험지 문서가 없습니다: 시험지 초안 ID={}", examDraftId);
        }
        
        // 3단계: 시험지 초안과 관련된 모든 데이터 삭제 (CASCADE로 자동 삭제됨)
        examDraftRepository.delete(examDraft);
        log.info("시험지 초안 및 관련 데이터 삭제 완료: 시험지 초안 ID={}", examDraftId);
    }
    
    /**
     * 답안지 생성
     */
    private ExamDocument createAnswerSheet(ExamDraft examDraft, List<ExamDraftQuestion> questions) {
        StringBuilder content = new StringBuilder();
        
        // 답안지 헤더
        content.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>");
        content.append("<h1 style='text-align: center; color: #333;'>").append(examDraft.getExamName()).append("</h1>");
        content.append("<h2 style='text-align: center; color: #666;'>답안지</h2>");
        content.append("<hr style='margin: 20px 0;'>");
        
        // 학생 정보 입력란
        content.append("<div style='margin-bottom: 30px;'>");
        content.append("<p><strong>학년:</strong> ").append(examDraft.getGrade()).append("학년</p>");
        content.append("<p><strong>이름:</strong> _________________</p>");
        content.append("<p><strong>전화번호:</strong> _________________</p>");
        content.append("</div>");
        
        // QR 코드 영역 (답안지에만)
        String qrCodeUrl = generateQrCodeUrl(examDraft.getId());
        content.append("<div style='text-align: center; margin: 20px 0;'>");
        content.append("<p><strong>QR 코드:</strong></p>");
        content.append("<img src='").append(qrCodeUrl).append("' alt='QR Code' style='width: 150px; height: 150px;'>");
        content.append("</div>");
        
        // 답안 작성란
        content.append("<div style='margin-top: 30px;'>");
        content.append("<h3>답안 작성란</h3>");
        
        for (ExamDraftQuestion question : questions) {
            content.append("<div style='margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px;'>");
            content.append("<p><strong>").append(question.getSeqNo()).append("번</strong> (배점: ").append(question.getPoints()).append("점)</p>");
            content.append("<div style='margin: 10px 0; padding: 10px; background-color: #f9f9f9; min-height: 50px;'>");
            content.append("답안: _________________________________");
            content.append("</div>");
            content.append("</div>");
        }
        
        content.append("</div>");
        content.append("</div>");
        
        return ExamDocument.builder()
            .examDraft(examDraft)
            .documentType(ExamDocument.DocumentType.ANSWER_SHEET)
            .documentContent(content.toString())
            .qrCodeUrl(qrCodeUrl)
            .build();
    }
    
    /**
     * 문제지 생성
     */
    private ExamDocument createQuestionPaper(ExamDraft examDraft, List<ExamDraftQuestion> questions) {
        StringBuilder content = new StringBuilder();
        
        // 문제지 헤더
        content.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>");
        content.append("<h1 style='text-align: center; color: #333;'>").append(examDraft.getExamName()).append("</h1>");
        content.append("<h2 style='text-align: center; color: #666;'>문제지</h2>");
        content.append("<hr style='margin: 20px 0;'>");
        
        // 시험 정보
        content.append("<div style='margin-bottom: 30px;'>");
        content.append("<p><strong>학년:</strong> ").append(examDraft.getGrade()).append("학년</p>");
        content.append("<p><strong>총 문제 수:</strong> ").append(examDraft.getTotalQuestions()).append("문제</p>");
        content.append("<p><strong>총점:</strong> 100점</p>");
        content.append("</div>");
        
        // 문제들
        content.append("<div style='margin-top: 30px;'>");
        content.append("<h3>문제</h3>");
        
        for (ExamDraftQuestion question : questions) {
            content.append("<div style='margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px;'>");
            content.append("<p><strong>").append(question.getSeqNo()).append("번</strong> (배점: ").append(question.getPoints()).append("점)</p>");
            content.append("<div style='margin: 10px 0;'>");
            content.append(question.getQuestion().getStem());
            content.append("</div>");
            content.append("</div>");
        }
        
        content.append("</div>");
        content.append("</div>");
        
        return ExamDocument.builder()
            .examDraft(examDraft)
            .documentType(ExamDocument.DocumentType.QUESTION_PAPER)
            .documentContent(content.toString())
            .build();
    }
    
    /**
     * 답안 생성
     */
    private ExamDocument createAnswerKey(ExamDraft examDraft, List<ExamDraftQuestion> questions) {
        StringBuilder content = new StringBuilder();
        
        // 답안 헤더
        content.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>");
        content.append("<h1 style='text-align: center; color: #333;'>").append(examDraft.getExamName()).append("</h1>");
        content.append("<h2 style='text-align: center; color: #666;'>답안</h2>");
        content.append("<hr style='margin: 20px 0;'>");
        
        // 시험 정보
        content.append("<div style='margin-bottom: 30px;'>");
        content.append("<p><strong>학년:</strong> ").append(examDraft.getGrade()).append("학년</p>");
        content.append("<p><strong>총 문제 수:</strong> ").append(examDraft.getTotalQuestions()).append("문제</p>");
        content.append("<p><strong>총점:</strong> 100점</p>");
        content.append("</div>");
        
        // 답안들
        content.append("<div style='margin-top: 30px;'>");
        content.append("<h3>답안</h3>");
        
        for (ExamDraftQuestion question : questions) {
            content.append("<div style='margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px;'>");
            content.append("<p><strong>").append(question.getSeqNo()).append("번</strong> (배점: ").append(question.getPoints()).append("점)</p>");
            content.append("<div style='margin: 10px 0;'>");
            content.append("<p><strong>정답:</strong> ").append(question.getQuestion().getAnswerKey()).append("</p>");
            content.append("</div>");
            content.append("</div>");
        }
        
        content.append("</div>");
        content.append("</div>");
        
        return ExamDocument.builder()
            .examDraft(examDraft)
            .documentType(ExamDocument.DocumentType.ANSWER_KEY)
            .documentContent(content.toString())
            .build();
    }
    
    /**
     * QR 코드 URL 생성
     */
    private String generateQrCodeUrl(Long examDraftId) {
        // 실제로는 QR 코드 이미지를 생성하고 저장해야 하지만,
        // 여기서는 임시 URL을 반환합니다.
        return "https://example.com/qr/" + examDraftId + "/" + UUID.randomUUID().toString();
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
