package com.iroomclass.springbackend.domain.user.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.user.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.user.dto.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.user.dto.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.user.dto.ExamSubmissionListResponse;
import com.iroomclass.springbackend.domain.user.dto.ExamSubmissionDetailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 제출 관리 서비스
 * 
 * 시험 제출 생성, 조회, 관리 기능을 제공합니다.
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
    
    /**
     * 시험별 제출 목록 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 목록
     */
    public ExamSubmissionListResponse getExamSubmissions(Long examId) {
        log.info("시험별 제출 목록 조회 요청: 시험 ID={}", examId);
        
        // 1단계: 시험 존재 확인
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));
        
        // 2단계: 제출 목록 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamIdOrderBySubmittedAtDesc(examId);
        
        List<ExamSubmissionListResponse.SubmissionInfo> submissionInfos = new ArrayList<>();
        for (ExamSubmission submission : submissions) {
            ExamSubmissionListResponse.SubmissionInfo submissionInfo = ExamSubmissionListResponse.SubmissionInfo.builder()
                .submissionId(submission.getId())
                .studentName(submission.getStudentName())
                .studentPhone(submission.getStudentPhone())
                .submittedAt(submission.getSubmittedAt())
                .totalScore(submission.getTotalScore())
                .build();
            submissionInfos.add(submissionInfo);
        }
        
        log.info("시험별 제출 목록 조회 완료: 시험={}, 제출={}개", exam.getExamName(), submissionInfos.size());
        
        return ExamSubmissionListResponse.builder()
            .examId(exam.getId())
            .examName(exam.getExamName())
            .grade(exam.getGrade())
            .submissions(submissionInfos)
            .totalCount(submissionInfos.size())
            .build();
    }
    
    /**
     * 시험 제출 상세 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 시험 제출 상세 정보
     */
    public ExamSubmissionDetailResponse getExamSubmissionDetail(Long submissionId) {
        log.info("시험 제출 상세 조회 요청: 제출 ID={}", submissionId);
        
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        Exam exam = submission.getExam();
        
        log.info("시험 제출 상세 조회 완료: 학생={}, 시험={}", submission.getStudentName(), exam.getExamName());
        
        return ExamSubmissionDetailResponse.builder()
            .submissionId(submission.getId())
            .examId(exam.getId())
            .examName(exam.getExamName())
            .grade(exam.getGrade())
            .studentName(submission.getStudentName())
            .studentPhone(submission.getStudentPhone())
            .submittedAt(submission.getSubmittedAt())
            .totalScore(submission.getTotalScore())
            .qrCodeUrl(exam.getQrCodeUrl())
            .build();
    }
    
    /**
     * 학생별 제출 목록 조회
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 목록
     */
    public List<ExamSubmissionDetailResponse> getStudentSubmissions(String studentName, String studentPhone) {
        log.info("학생별 제출 목록 조회 요청: 학생={}, 전화번호={}", studentName, studentPhone);
        
        List<ExamSubmission> submissions = examSubmissionRepository.findByStudentNameAndStudentPhone(studentName, studentPhone);
        
        List<ExamSubmissionDetailResponse> submissionDetails = new ArrayList<>();
        for (ExamSubmission submission : submissions) {
            Exam exam = submission.getExam();
            ExamSubmissionDetailResponse detail = ExamSubmissionDetailResponse.builder()
                .submissionId(submission.getId())
                .examId(exam.getId())
                .examName(exam.getExamName())
                .grade(exam.getGrade())
                .studentName(submission.getStudentName())
                .studentPhone(submission.getStudentPhone())
                .submittedAt(submission.getSubmittedAt())
                .totalScore(submission.getTotalScore())
                .qrCodeUrl(exam.getQrCodeUrl())
                .build();
            submissionDetails.add(detail);
        }
        
        log.info("학생별 제출 목록 조회 완료: 학생={}, 제출={}개", studentName, submissionDetails.size());
        
        return submissionDetails;
    }
    
    /**
     * 시험별 제출 학생 수 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 학생 수
     */
    public long getExamSubmissionCount(Long examId) {
        long count = examSubmissionRepository.countByExamId(examId);
        log.info("시험별 제출 학생 수 조회: 시험 ID={}, 제출 학생 수={}", examId, count);
        return count;
    }
}
