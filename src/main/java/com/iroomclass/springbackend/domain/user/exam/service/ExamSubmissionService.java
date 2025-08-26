package com.iroomclass.springbackend.domain.user.exam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.user.exam.dto.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;

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
    

}
