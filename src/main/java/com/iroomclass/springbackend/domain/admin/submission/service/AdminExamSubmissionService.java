package com.iroomclass.springbackend.domain.admin.submission.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.submission.dto.ExamSubmissionDetailResponse;
import com.iroomclass.springbackend.domain.admin.submission.dto.ExamSubmissionListResponse;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.user.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 시험 제출 관리 서비스
 * 
 * 관리자가 시험 제출 현황을 조회하고 관리할 수 있는 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminExamSubmissionService {
    
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamRepository examRepository;
    
    /**
     * 시험별 제출 목록 조회
     * 
     * @param examId 시험 ID
     * @return 해당 시험의 제출 목록
     */
    public ExamSubmissionListResponse getExamSubmissions(Long examId) {
        log.info("관리자 - 시험별 제출 목록 조회 요청: 시험 ID={}", examId);
        
        // 1단계: 시험 존재 확인
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));
        
        // 2단계: 제출 목록 조회
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamIdOrderBySubmittedAtDesc(examId);
        
        List<ExamSubmissionListResponse.SubmissionInfo> submissionInfos = new ArrayList<>();
        for (ExamSubmission submission : submissions) {
            ExamSubmissionListResponse.SubmissionInfo submissionInfo = new ExamSubmissionListResponse.SubmissionInfo(submission.getId(), submission.getStudentName(), submission.getStudentPhone(), submission.getSubmittedAt(), submission.getTotalScore());
            submissionInfos.add(submissionInfo);
        }
        
        log.info("관리자 - 시험별 제출 목록 조회 완료: 시험={}, 제출={}개", exam.getExamName(), submissionInfos.size());
        
        return new ExamSubmissionListResponse(exam.getId(), exam.getExamName(), exam.getGrade() + "학년", submissionInfos, submissionInfos.size());
    }
    
    /**
     * 시험 제출 상세 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 시험 제출 상세 정보
     */
    public ExamSubmissionDetailResponse getExamSubmissionDetail(Long submissionId) {
        log.info("관리자 - 시험 제출 상세 조회 요청: 제출 ID={}", submissionId);
        
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));
        
        Exam exam = submission.getExam();
        
        log.info("관리자 - 시험 제출 상세 조회 완료: 학생={}, 시험={}", submission.getStudentName(), exam.getExamName());
        
        return new ExamSubmissionDetailResponse(submission.getId(), exam.getId(), exam.getExamName(), exam.getGrade() + "학년", submission.getStudentName(), submission.getStudentPhone(), submission.getSubmittedAt(), submission.getTotalScore(), exam.getQrCodeUrl());
    }
    
    /**
     * 학생별 제출 목록 조회
     * 
     * @param studentName 학생 이름
     * @param studentPhone 학생 전화번호
     * @return 해당 학생의 제출 목록
     */
    public List<ExamSubmissionDetailResponse> getStudentSubmissions(String studentName, String studentPhone) {
        log.info("관리자 - 학생별 제출 목록 조회 요청: 학생={}, 전화번호={}", studentName, studentPhone);
        
        List<ExamSubmission> submissions = examSubmissionRepository.findByStudentNameAndStudentPhone(studentName, studentPhone);
        
        List<ExamSubmissionDetailResponse> submissionDetails = new ArrayList<>();
        for (ExamSubmission submission : submissions) {
            Exam exam = submission.getExam();
            ExamSubmissionDetailResponse detail = new ExamSubmissionDetailResponse(submission.getId(), exam.getId(), exam.getExamName(), exam.getGrade() + "학년", submission.getStudentName(), submission.getStudentPhone(), submission.getSubmittedAt(), submission.getTotalScore(), exam.getQrCodeUrl());
            submissionDetails.add(detail);
        }
        
        log.info("관리자 - 학생별 제출 목록 조회 완료: 학생={}, 제출={}개", studentName, submissionDetails.size());
        
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
        log.info("관리자 - 시험별 제출 학생 수 조회: 시험 ID={}, 제출 학생 수={}", examId, count);
        return count;
    }
}
