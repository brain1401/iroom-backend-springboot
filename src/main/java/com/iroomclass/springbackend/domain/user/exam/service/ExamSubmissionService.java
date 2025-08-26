package com.iroomclass.springbackend.domain.user.exam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.user.exam.answer.repository.ExamAnswerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 제출 관리 서비스 (학생용)
 * 
 * 학생이 시험을 제출할 수 있는 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamSubmissionService {
    
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    
    /**
     * 시험 제출 생성
     * 
     * @param request 시험 제출 생성 요청
     * @return 생성된 시험 제출 정보
     */
    @Transactional
    public ExamSubmissionCreateResponse createExamSubmission(ExamSubmissionCreateRequest request) {
        log.info("시험 제출 생성 요청: 시험={}, 학생={}, 전화번호={}", 
            request.getExamId(), request.getStudentName(), request.getStudentPhone());
        
        // 1단계: 시험 존재 확인
        Exam exam = examRepository.findById(request.getExamId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + request.getExamId()));
        
        // 2단계: 중복 제출 방지
        if (examSubmissionRepository.existsByExamIdAndStudentNameAndStudentPhone(
                request.getExamId(), request.getStudentName(), request.getStudentPhone())) {
            throw new IllegalArgumentException("이미 제출한 시험입니다.");
        }
        
        // 3단계: 시험 제출 생성
        ExamSubmission submission = ExamSubmission.builder()
            .exam(exam)
            .studentName(request.getStudentName())
            .studentPhone(request.getStudentPhone())
            .build();
        
        submission = examSubmissionRepository.save(submission);
        
        log.info("시험 제출 생성 완료: ID={}, 학생={}, 시험={}", 
            submission.getId(), request.getStudentName(), exam.getExamName());
        
        return ExamSubmissionCreateResponse.builder()
            .submissionId(submission.getId())
            .examId(exam.getId())
            .examName(exam.getExamName())
            .studentName(submission.getStudentName())
            .studentPhone(submission.getStudentPhone())
            .submittedAt(submission.getSubmittedAt())
            .qrCodeUrl(exam.getQrCodeUrl())
            .build();
    }
    
    /**
     * 시험 최종 제출
     * 
     * @param submissionId 시험 제출 ID
     * @return 최종 제출 완료 정보
     */
    @Transactional
    public ExamSubmissionCreateResponse finalSubmitExam(Long submissionId) {
        log.info("시험 최종 제출 요청: 제출 ID={}", submissionId);
        
        // 1단계: 시험 제출 존재 확인
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        // 2단계: 답안 완료 여부 확인
        long answerCount = examAnswerRepository.countByExamSubmissionId(submissionId);
        if (answerCount == 0) {
            throw new IllegalArgumentException("답안이 완료되지 않았습니다. 답안을 먼저 작성해주세요.");
        }
        
        // 3단계: 최종 제출 처리 (submittedAt 업데이트)
        submission.updateSubmittedAt();
        submission = examSubmissionRepository.save(submission);
        
        log.info("시험 최종 제출 완료: 제출 ID={}, 학생={}, 답안 수={}", 
            submission.getId(), submission.getStudentName(), answerCount);
        
        return ExamSubmissionCreateResponse.builder()
            .submissionId(submission.getId())
            .examId(submission.getExam().getId())
            .examName(submission.getExam().getExamName())
            .studentName(submission.getStudentName())
            .studentPhone(submission.getStudentPhone())
            .submittedAt(submission.getSubmittedAt())
            .qrCodeUrl(submission.getExam().getQrCodeUrl())
            .build();
    }
}
