package com.iroomclass.springbackend.domain.exam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult.ResultStatus;
import com.iroomclass.springbackend.domain.exam.repository.ExamResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 결과 서비스
 * 
 * AI 기반 시험 채점 결과 관련 비즈니스 로직을 처리합니다.
 * AI 자동 채점 및 재채점 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExamResultService {
    
    private final ExamResultRepository examResultRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamSheetRepository examSheetRepository;
    private final QuestionResultService questionResultService;
    
    /**
     * 시험 제출에 대한 AI 자동 채점 시작
     * 
     * @param submissionId 시험 제출 ID
     * @return 생성된 시험 결과
     * @throws IllegalArgumentException 제출물이 존재하지 않을 때
     */
    @Transactional
    public ExamResult startAutoGrading(UUID submissionId) {
        log.info("AI 자동 채점 시작: submissionId={}", submissionId);
        
        try {
            log.debug("Step 1: ExamSubmission 조회 시작");
            ExamSubmission submission = examSubmissionRepository.findByIdWithExamAndExamSheet(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("시험 제출물을 찾을 수 없습니다: " + submissionId));
            log.debug("Step 1 완료: ExamSubmission 조회 성공, examId={}", submission.getExam().getId());
            
            log.debug("Step 2: 기존 채점 결과 확인 시작");
            // 이미 채점이 진행 중인지 확인
            if (examResultRepository.existsByExamSubmissionId(submissionId)) {
                ExamResult existingResult = examResultRepository.findLatestBySubmissionId(submissionId)
                    .orElseThrow();
                
                if (existingResult.getStatus() == ResultStatus.IN_PROGRESS) {
                    log.warn("이미 채점이 진행 중입니다: submissionId={}", submissionId);
                    return existingResult;
                }
            }
            log.debug("Step 2 완료: 기존 채점 결과 확인 완료");
            
            log.debug("Step 3: ExamSheet 조회 시작");
            // ExamSubmission의 Exam에서 직접 ExamSheet 가져오기
            ExamSheet examSheet = submission.getExam().getExamSheet();
            if (examSheet == null) {
                throw new IllegalArgumentException("시험에 연결된 시험지를 찾을 수 없습니다: examId=" + submission.getExam().getId());
            }
            log.debug("Step 3 완료: ExamSheet 조회 성공, examSheetId={}", examSheet.getId());
            
            log.debug("Step 4: ExamResult 엔티티 생성 시작");
            // 새로운 AI 채점 결과 생성
            ExamResult examResult = ExamResult.builder()
                .examSubmission(submission)
                .examSheet(examSheet)
                .status(ResultStatus.IN_PROGRESS)
                .build();
            log.debug("Step 4 완료: ExamResult 엔티티 생성 완료");
            
            log.debug("Step 5: ExamResult 저장 시작");
            ExamResult savedResult = examResultRepository.save(examResult);
            log.info("시험 결과 생성 완료: resultId={}", savedResult.getId());
            log.debug("Step 5 완료: ExamResult 저장 완료");
            
            log.debug("Step 6: QuestionResult 자동 채점 시작");
            // 문제별 AI 자동 채점 시작
            questionResultService.startAutoGradingForSubmission(savedResult, submission);
            log.debug("Step 6 완료: QuestionResult 자동 채점 완료");
            
            log.info("AI 자동 채점 전체 프로세스 완료: submissionId={}, resultId={}", submissionId, savedResult.getId());
            return savedResult;
            
        } catch (Exception e) {
            log.error("AI 자동 채점 중 오류 발생: submissionId={}, error={}", submissionId, e.getMessage(), e);
            throw e;
        }
    }
    
    
    /**
     * AI 재채점 시작
     * 
     * @param originalResultId 기존 채점 결과 ID
     * @return 재채점용 새 시험 결과
     * @throws IllegalArgumentException 기존 결과가 존재하지 않을 때
     */
    @Transactional
    public ExamResult startRegrading(UUID originalResultId) {
        log.info("AI 재채점 시작: originalResultId={}", originalResultId);
        
        ExamResult originalResult = examResultRepository.findById(originalResultId)
            .orElseThrow(() -> new IllegalArgumentException("기존 채점 결과를 찾을 수 없습니다: " + originalResultId));
        
        // 기존 결과 상태 업데이트
        originalResult.updateStatus(ResultStatus.REGRADED);
        examResultRepository.save(originalResult);
        
        // 새로운 버전의 AI 채점 결과 생성
        ExamResult newResult = originalResult.createNewVersionForRegrading();
        
        // AI 재채점 시작 상태로 변경
        newResult.updateStatus(ResultStatus.IN_PROGRESS);
        ExamResult savedResult = examResultRepository.save(newResult);
        
        log.info("AI 재채점 결과 생성 완료: newResultId={}, version={}", savedResult.getId(), savedResult.getVersion());
        
        // 문제별 AI 재채점 준비
        questionResultService.prepareRegrading(savedResult, originalResult);
        
        // 객관식 문제에 대한 자동 채점 실행
        questionResultService.executeAutoGradingForRegrading(savedResult);
        
        return savedResult;
    }
    
    /**
     * 채점 완료 처리
     * 
     * @param resultId 시험 결과 ID
     * @param comment 채점 코멘트
     * @throws IllegalArgumentException 시험 결과가 존재하지 않을 때
     */
    @Transactional
    public void completeGrading(UUID resultId, String comment) {
        log.info("채점 완료 처리: resultId={}", resultId);
        
        ExamResult examResult = examResultRepository.findById(resultId)
            .orElseThrow(() -> new IllegalArgumentException("시험 결과를 찾을 수 없습니다: " + resultId));
        
        // 모든 문제가 채점되었는지 확인
        boolean allGraded = questionResultService.isAllQuestionsGraded(resultId);
        if (!allGraded) {
            throw new IllegalStateException("아직 채점되지 않은 문제가 있습니다: " + resultId);
        }
        
        // 총점 계산 및 업데이트
        examResult.calculateAndUpdateTotalScore();
        
        // 채점 완료 상태로 변경 (재채점인 경우 REGRADED, 아닌 경우 COMPLETED)
        ResultStatus newStatus = examResult.getVersion() > 1 ? ResultStatus.REGRADED : ResultStatus.COMPLETED;
        examResult.updateStatus(newStatus);
        examResult.updateScoringComment(comment);
        
        examResultRepository.save(examResult);
        log.info("채점 완료: resultId={}, totalScore={}", resultId, examResult.getTotalScore());
    }
    
    /**
     * 제출 ID로 최신 채점 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 최신 채점 결과
     */
    public ExamResult findLatestResultBySubmissionId(UUID submissionId) {
        return examResultRepository.findLatestBySubmissionId(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("채점 결과를 찾을 수 없습니다: " + submissionId));
    }
    
    /**
     * 제출 ID로 모든 채점 히스토리 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 채점 히스토리 목록
     */
    public List<ExamResult> findAllResultsBySubmissionId(UUID submissionId) {
        return examResultRepository.findAllBySubmissionIdOrderByVersionDesc(submissionId);
    }
    
    /**
     * ID로 시험 결과 조회
     * 
     * @param resultId 시험 결과 ID
     * @return 시험 결과
     */
    public ExamResult findById(UUID resultId) {
        return examResultRepository.findById(resultId)
            .orElseThrow(() -> new IllegalArgumentException("시험 결과를 찾을 수 없습니다: " + resultId));
    }
    
    
    /**
     * AI 자동 채점 결과 조회
     * 
     * @param pageable 페이징 정보
     * @return AI 자동 채점 결과 페이지
     */
    public Page<ExamResult> findAutoGradedResults(Pageable pageable) {
        return examResultRepository.findAutoGradedResults(pageable);
    }
    
    /**
     * 채점 상태별 결과 조회
     * 
     * @param status 채점 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 채점 결과 페이지
     */
    public Page<ExamResult> findResultsByStatus(ResultStatus status, Pageable pageable) {
        return examResultRepository.findByStatusOrderByGradedAtDesc(status, pageable);
    }
    
    /**
     * 특정 기간 내 채점 결과 조회
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @param pageable 페이징 정보
     * @return 기간 내 채점 결과 페이지
     */
    public Page<ExamResult> findResultsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return examResultRepository.findByGradedAtBetween(startDate, endDate, pageable);
    }
    
    /**
     * 재채점 결과 조회
     * 
     * @param pageable 페이징 정보
     * @return 재채점 결과 페이지
     */
    public Page<ExamResult> findRegradedResults(Pageable pageable) {
        return examResultRepository.findRegradedResults(pageable);
    }
    
    /**
     * 점수 범위별 결과 조회
     * 
     * @param minScore 최소 점수
     * @param maxScore 최대 점수
     * @param pageable 페이징 정보
     * @return 점수 범위 내 채점 결과 페이지
     */
    public Page<ExamResult> findResultsByScoreRange(Integer minScore, Integer maxScore, Pageable pageable) {
        return examResultRepository.findByTotalScoreBetween(minScore, maxScore, pageable);
    }
    
    /**
     * 채점 상태별 개수 조회
     * 
     * @param status 채점 상태
     * @return 해당 상태의 채점 결과 개수
     */
    public long countByStatus(ResultStatus status) {
        return examResultRepository.countByStatus(status);
    }
    
    
    /**
     * AI 자동 채점 개수 조회
     * 
     * @return AI 자동 채점 개수
     */
    public long countAutoGradedResults() {
        return examResultRepository.countAutoGradedResults();
    }
    
    /**
     * 채점 결과 삭제
     * 
     * @param resultId 시험 결과 ID
     * @throws IllegalArgumentException 시험 결과가 존재하지 않을 때
     */
    @Transactional
    public void deleteResult(UUID resultId) {
        log.info("채점 결과 삭제: resultId={}", resultId);
        
        if (!examResultRepository.existsById(resultId)) {
            throw new IllegalArgumentException("시험 결과를 찾을 수 없습니다: " + resultId);
        }
        
        examResultRepository.deleteById(resultId);
        log.info("채점 결과 삭제 완료: resultId={}", resultId);
    }
}