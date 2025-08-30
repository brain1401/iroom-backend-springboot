package com.iroomclass.springbackend.domain.user.exam.result.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.info.entity.Admin;
import com.iroomclass.springbackend.domain.admin.info.repository.AdminRepository;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult.ResultStatus;
import com.iroomclass.springbackend.domain.user.exam.result.repository.ExamResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 결과 서비스
 * 
 * 시험 채점 결과 관련 비즈니스 로직을 처리합니다.
 * 자동 채점, 수동 채점, 재채점 등의 기능을 제공합니다.
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
    private final AdminRepository adminRepository;
    private final QuestionResultService questionResultService;
    
    /**
     * 시험 제출에 대한 자동 채점 시작
     * 
     * @param submissionId 시험 제출 ID
     * @return 생성된 시험 결과
     * @throws IllegalArgumentException 제출물이 존재하지 않을 때
     */
    @Transactional
    public ExamResult startAutoGrading(UUID submissionId) {
        log.info("자동 채점 시작: submissionId={}", submissionId);
        
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("시험 제출물을 찾을 수 없습니다: " + submissionId));
        
        // 이미 채점이 진행 중인지 확인
        if (examResultRepository.existsByExamSubmissionId(submissionId)) {
            ExamResult existingResult = examResultRepository.findLatestBySubmissionId(submissionId)
                .orElseThrow();
            
            if (existingResult.getStatus() == ResultStatus.IN_PROGRESS) {
                log.warn("이미 채점이 진행 중입니다: submissionId={}", submissionId);
                return existingResult;
            }
        }
        
        // 새로운 채점 결과 생성
        ExamResult examResult = ExamResult.builder()
            .examSubmission(submission)
            .gradedBy(null) // 자동 채점
            .status(ResultStatus.IN_PROGRESS)
            .build();
        
        ExamResult savedResult = examResultRepository.save(examResult);
        log.info("시험 결과 생성 완료: resultId={}", savedResult.getId());
        
        // 문제별 자동 채점 시작
        questionResultService.startAutoGradingForSubmission(savedResult, submission);
        
        return savedResult;
    }
    
    /**
     * 수동 채점 시작
     * 
     * @param submissionId 시험 제출 ID
     * @param graderId 채점자 ID
     * @return 생성된 시험 결과
     * @throws IllegalArgumentException 제출물 또는 채점자가 존재하지 않을 때
     */
    @Transactional
    public ExamResult startManualGrading(UUID submissionId, UUID graderId) {
        log.info("수동 채점 시작: submissionId={}, graderId={}", submissionId, graderId);
        
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("시험 제출물을 찾을 수 없습니다: " + submissionId));
        
        Admin grader = adminRepository.findById(graderId)
            .orElseThrow(() -> new IllegalArgumentException("채점자를 찾을 수 없습니다: " + graderId));
        
        // 새로운 채점 결과 생성
        ExamResult examResult = ExamResult.builder()
            .examSubmission(submission)
            .gradedBy(grader)
            .status(ResultStatus.IN_PROGRESS)
            .build();
        
        ExamResult savedResult = examResultRepository.save(examResult);
        log.info("수동 채점 시작 완료: resultId={}", savedResult.getId());
        
        // 문제별 수동 채점 준비
        questionResultService.prepareManualGrading(savedResult, submission);
        
        return savedResult;
    }
    
    /**
     * 재채점 시작
     * 
     * @param originalResultId 기존 채점 결과 ID
     * @param graderId 새로운 채점자 ID
     * @return 재채점용 새 시험 결과
     * @throws IllegalArgumentException 기존 결과 또는 채점자가 존재하지 않을 때
     */
    @Transactional
    public ExamResult startRegrading(UUID originalResultId, UUID graderId) {
        log.info("재채점 시작: originalResultId={}, graderId={}", originalResultId, graderId);
        
        ExamResult originalResult = examResultRepository.findById(originalResultId)
            .orElseThrow(() -> new IllegalArgumentException("기존 채점 결과를 찾을 수 없습니다: " + originalResultId));
        
        Admin grader = adminRepository.findById(graderId)
            .orElseThrow(() -> new IllegalArgumentException("채점자를 찾을 수 없습니다: " + graderId));
        
        // 기존 결과 상태 업데이트
        originalResult.updateStatus(ResultStatus.REGRADED);
        examResultRepository.save(originalResult);
        
        // 새로운 버전의 채점 결과 생성
        ExamResult newResult = originalResult.createNewVersionForRegrading(grader);
        ExamResult savedResult = examResultRepository.save(newResult);
        
        log.info("재채점 결과 생성 완료: newResultId={}, version={}", savedResult.getId(), savedResult.getVersion());
        
        // 문제별 재채점 준비
        questionResultService.prepareRegrading(savedResult, originalResult);
        
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
        
        // 채점 완료 상태로 변경
        examResult.updateStatus(ResultStatus.COMPLETED);
        examResult.updateGradingComment(comment);
        
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
     * 채점자별 결과 조회
     * 
     * @param graderId 채점자 ID
     * @param pageable 페이징 정보
     * @return 채점 결과 페이지
     */
    public Page<ExamResult> findResultsByGrader(UUID graderId, Pageable pageable) {
        return examResultRepository.findByGraderId(graderId, pageable);
    }
    
    /**
     * 자동 채점 결과 조회
     * 
     * @param pageable 페이징 정보
     * @return 자동 채점 결과 페이지
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
     * 채점자별 채점 개수 조회
     * 
     * @param graderId 채점자 ID
     * @return 채점 개수
     */
    public long countByGrader(UUID graderId) {
        return examResultRepository.countByGraderId(graderId);
    }
    
    /**
     * 자동 채점 개수 조회
     * 
     * @return 자동 채점 개수
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