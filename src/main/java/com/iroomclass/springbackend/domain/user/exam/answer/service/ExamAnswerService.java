package com.iroomclass.springbackend.domain.user.exam.answer.service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerCreateRequest;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerListResponse;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerResponse;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerUpdateRequest;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerSheetCreateRequest;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.ExamAnswerSheetProcessResponse;
import com.iroomclass.springbackend.domain.user.exam.answer.dto.RecognizedAnswer;
import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.ExamAnswerRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.domain.admin.question.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDraftQuestion;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDraftQuestionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 답안 관리 서비스
 * 
 * 답안 생성, 수정, 조회 등의 기능을 제공합니다.
 * AI 이미지 인식과 연동하여 답안을 처리합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamAnswerService {
    
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final QuestionRepository questionRepository;
    private final ExamDraftQuestionRepository examDraftQuestionRepository;
    private final AiImageRecognitionService aiImageRecognitionService;
    
    /**
     * 답안 생성 (AI 이미지 인식 포함)
     * 
     * @param request 답안 생성 요청
     * @return 생성된 답안 정보
     */
    @Transactional
    public ExamAnswerResponse createExamAnswer(ExamAnswerCreateRequest request) {
        log.info("답안 생성 요청: 제출 ID={}, 문제 ID={}, 이미지 URL={}", 
            request.getExamSubmissionId(), request.getQuestionId(), request.getAnswerImageUrl());
        
        // 1단계: 시험 제출 존재 확인
        ExamSubmission examSubmission = examSubmissionRepository.findById(request.getExamSubmissionId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + request.getExamSubmissionId()));
        
        // 2단계: 문제 존재 확인
        Question question = questionRepository.findById(request.getQuestionId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제입니다: " + request.getQuestionId()));
        
        // 3단계: 중복 답안 방지
        if (examAnswerRepository.existsByExamSubmissionIdAndQuestionId(
                request.getExamSubmissionId(), request.getQuestionId())) {
            throw new IllegalArgumentException("이미 답안이 존재하는 문제입니다: " + request.getQuestionId());
        }
        
        // 4단계: 답안 생성
        ExamAnswer examAnswer = ExamAnswer.builder()
            .examSubmission(examSubmission)
            .question(question)
            .answerImageUrl(request.getAnswerImageUrl())
            .build();
        
        examAnswer = examAnswerRepository.save(examAnswer);
        
        // 5단계: AI 이미지 인식 수행
        try {
            AiImageRecognitionService.AiRecognitionResult recognitionResult = 
                aiImageRecognitionService.recognizeTextFromImage(request.getAnswerImageUrl());
            
            examAnswer.updateAnswerText(recognitionResult.getRecognizedText());
            examAnswer = examAnswerRepository.save(examAnswer);
            
            log.info("답안 생성 및 AI 인식 완료: 답안 ID={}, 인식 결과={}", 
                examAnswer.getId(), recognitionResult.getRecognizedText());
                
        } catch (Exception e) {
            log.error("AI 인식 실패: 답안 ID={}, 오류={}", examAnswer.getId(), e.getMessage());
        }
        
        return ExamAnswerResponse.from(examAnswer);
    }
    
    /**
     * 답안 수정 (재촬영)
     * 
     * @param answerId 답안 ID
     * @param newImageUrl 새로운 이미지 URL
     * @return 수정된 답안 정보
     */
    @Transactional
    public ExamAnswerResponse retakeExamAnswer(Long answerId, String newImageUrl) {
        log.info("답안 재촬영 요청: 답안 ID={}, 새 이미지 URL={}", answerId, newImageUrl);
        
        ExamAnswer examAnswer = examAnswerRepository.findById(answerId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답안입니다: " + answerId));
        
        // 재촬영 처리
        examAnswer.updateImageUrl(newImageUrl);
        examAnswer = examAnswerRepository.save(examAnswer);
        
        // AI 이미지 인식 수행
        try {
            AiImageRecognitionService.AiRecognitionResult recognitionResult = 
                aiImageRecognitionService.recognizeTextFromImage(newImageUrl);
            
            examAnswer.updateAnswerText(recognitionResult.getRecognizedText());
            examAnswer = examAnswerRepository.save(examAnswer);
            
            log.info("답안 재촬영 및 AI 인식 완료: 답안 ID={}, 인식 결과={}", 
                examAnswer.getId(), recognitionResult.getRecognizedText());
                
        } catch (Exception e) {
            log.error("AI 인식 실패: 답안 ID={}, 오류={}", examAnswer.getId(), e.getMessage());
        }
        
        return ExamAnswerResponse.from(examAnswer);
    }
    
    /**
     * 답안 수정 (텍스트 수정)
     * 
     * @param request 답안 수정 요청
     * @return 수정된 답안 정보
     */
    @Transactional
    public ExamAnswerResponse updateExamAnswer(ExamAnswerUpdateRequest request) {
        log.info("답안 수정 요청: 답안 ID={}, 수정된 답안={}", request.getAnswerId(), request.getAnswerText());
        
        ExamAnswer examAnswer = examAnswerRepository.findById(request.getAnswerId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답안입니다: " + request.getAnswerId()));
        
        // 1단계: 답안 텍스트 업데이트
        examAnswer.updateAnswerText(request.getAnswerText());
        
        // 2단계: 정답 여부 다시 확인
        Question question = examAnswer.getQuestion();
        if (question != null) {
            String correctAnswer = question.getAnswerKey();
            String studentAnswer = request.getAnswerText();
            
            // 정답 비교 (공백 제거 후 비교)
            boolean isCorrect = correctAnswer != null && 
                correctAnswer.trim().equalsIgnoreCase(studentAnswer.trim());
            
            // 3단계: ExamDraftQuestion에서 해당 문제의 배점 가져오기
            Integer score = 0;
            if (isCorrect) {
                try {
                    // ExamSubmission -> Exam -> ExamDraft -> ExamDraftQuestion 순서로 찾기
                    Long examDraftId = examAnswer.getExamSubmission().getExam().getExamDraft().getId();
                    Long questionId = question.getId();
                    
                    ExamDraftQuestion examDraftQuestion = examDraftQuestionRepository
                        .findByExamDraftIdAndQuestionId(examDraftId, questionId)
                        .orElse(null);
                    
                    if (examDraftQuestion != null) {
                        score = examDraftQuestion.getPoints();
                        log.info("ExamDraftQuestion에서 배점 조회: examDraftId={}, questionId={}, points={}", 
                            examDraftId, questionId, score);
                    } else {
                        score = 5; // 기본값
                        log.warn("ExamDraftQuestion을 찾을 수 없어 기본 배점 사용: examDraftId={}, questionId={}", 
                            examDraftId, questionId);
                    }
                } catch (Exception e) {
                    score = 5; // 기본값
                    log.error("배점 조회 중 오류 발생, 기본 배점 사용: {}", e.getMessage());
                }
            }
            
            // 채점 결과 업데이트
            examAnswer.updateGrading(isCorrect, score);
            
            log.info("답안 채점 완료: 답안 ID={}, 학생 답안={}, 정답={}, 정답 여부={}, 점수={}", 
                examAnswer.getId(), studentAnswer, correctAnswer, isCorrect, score);
        } else {
            log.warn("문제 정보를 찾을 수 없습니다: 답안 ID={}", examAnswer.getId());
        }
        
        examAnswer = examAnswerRepository.save(examAnswer);
        
        log.info("답안 수정 완료: 답안 ID={}", examAnswer.getId());
        
        return ExamAnswerResponse.from(examAnswer);
    }
    
    /**
     * 답안 목록 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 모든 답안 목록
     */
    public ExamAnswerListResponse getExamAnswers(Long examSubmissionId) {
        log.info("답안 목록 조회 요청: 시험 제출 ID={}", examSubmissionId);
        
        List<ExamAnswer> examAnswers = examAnswerRepository.findByExamSubmissionId(examSubmissionId);
        
        log.info("답안 목록 조회 완료: 시험 제출 ID={}, 답안 수={}", examSubmissionId, examAnswers.size());
        
        return ExamAnswerListResponse.from(examAnswers, examSubmissionId);
    }
    
    /**
     * 특정 문제 답안 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @param questionId 문제 ID
     * @return 해당 문제의 답안 정보
     */
    public ExamAnswerResponse getExamAnswer(Long examSubmissionId, Long questionId) {
        log.info("특정 문제 답안 조회 요청: 시험 제출 ID={}, 문제 ID={}", examSubmissionId, questionId);
        
        ExamAnswer examAnswer = examAnswerRepository.findByExamSubmissionIdAndQuestionId(examSubmissionId, questionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답안입니다: 시험 제출 ID=" + examSubmissionId + ", 문제 ID=" + questionId));
        
        log.info("특정 문제 답안 조회 완료: 답안 ID={}", examAnswer.getId());
        
        return ExamAnswerResponse.from(examAnswer);
    }
    
    /**
     * 답안 상태 요약
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 답안 상태 요약 정보
     */
    public AnswerStatusSummary getAnswerStatusSummary(Long examSubmissionId) {
        log.info("답안 상태 확인 요청: 시험 제출 ID={}", examSubmissionId);
        
        long totalCount = examAnswerRepository.countByExamSubmissionId(examSubmissionId);
        long correctCount = examAnswerRepository.countByExamSubmissionIdAndIsCorrectTrue(examSubmissionId);
        
        AnswerStatusSummary summary = AnswerStatusSummary.builder()
            .totalCount((int) totalCount)
            .correctCount((int) correctCount)
            .build();
        
        log.info("답안 상태 확인 완료: 총 {}개, 정답 {}개", totalCount, correctCount);
        
        return summary;
    }
    
    /**
     * 답안지 전체 촬영 처리
     * 
     * @param request 답안지 전체 촬영 요청
     * @return 처리 결과
     */
    @Transactional
    public ExamAnswerSheetProcessResponse processAnswerSheet(ExamAnswerSheetCreateRequest request) {
        log.info("답안지 전체 촬영 처리 시작: examSubmissionId={}, 이미지 개수={}", 
            request.getExamSubmissionId(), request.getAnswerSheetImageUrls().size());
        
        // 1단계: 시험 제출 정보 조회
        ExamSubmission examSubmission = examSubmissionRepository.findById(request.getExamSubmissionId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + request.getExamSubmissionId()));
        
        // 2단계: AI 이미지 인식 수행
        List<RecognizedAnswer> recognizedAnswers = aiImageRecognitionService.recognizeAnswersFromSheet(
            request.getAnswerSheetImageUrls()
        );
        
        // 3단계: 인식된 답안들을 DB에 저장
        List<ExamAnswer> createdAnswers = new ArrayList<>();
        for (RecognizedAnswer recognizedAnswer : recognizedAnswers) {
            // 해당 문제 조회
            Question question = questionRepository.findById(recognizedAnswer.getQuestionNumber().longValue())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제입니다: " + recognizedAnswer.getQuestionNumber()));
            
            // 답안 생성
            ExamAnswer examAnswer = ExamAnswer.builder()
                .examSubmission(examSubmission)
                .question(question)
                .answerImageUrl(request.getAnswerSheetImageUrls().get(0)) // 첫 번째 이미지 URL 사용
                .answerText(recognizedAnswer.getRecognizedAnswer())
                .isCorrect(false) // 아직 정답 여부 확인 안됨
                .score(0) // 아직 점수 계산 안됨
                .build();
            
            ExamAnswer savedAnswer = examAnswerRepository.save(examAnswer);
            createdAnswers.add(savedAnswer);
        }
        
        // 4단계: 응답 DTO 생성
        List<ExamAnswerResponse> answerResponses = createdAnswers.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        log.info("답안지 전체 촬영 처리 완료: examSubmissionId={}, 생성된 답안 개수={}", 
            request.getExamSubmissionId(), createdAnswers.size());
        
        return ExamAnswerSheetProcessResponse.builder()
            .processedImageCount(request.getAnswerSheetImageUrls().size())
            .createdAnswerCount(createdAnswers.size())
            .answers(answerResponses)
            .status("COMPLETED")
            .message("답안지 처리가 완료되었습니다. 각 문제별 답안을 확인해주세요.")
            .build();
    }
    
    /**
     * ExamAnswer를 ExamAnswerResponse로 변환
     * 
     * @param examAnswer 답안 엔티티
     * @return 답안 응답 DTO
     */
    private ExamAnswerResponse convertToResponse(ExamAnswer examAnswer) {
        return ExamAnswerResponse.from(examAnswer);
    }
    
    /**
     * 답안 상태 요약 클래스
     */
    public static class AnswerStatusSummary {
        private final int totalCount;
        private final int correctCount;
        
        public AnswerStatusSummary(int totalCount, int correctCount) {
            this.totalCount = totalCount;
            this.correctCount = correctCount;
        }
        
        public int getTotalCount() { return totalCount; }
        public int getCorrectCount() { return correctCount; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int totalCount;
            private int correctCount;
            
            public Builder totalCount(int totalCount) {
                this.totalCount = totalCount;
                return this;
            }
            
            public Builder correctCount(int correctCount) {
                this.correctCount = correctCount;
                return this;
            }
            
            public AnswerStatusSummary build() {
                return new AnswerStatusSummary(totalCount, correctCount);
            }
        }
    }
}
