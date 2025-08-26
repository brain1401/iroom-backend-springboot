package com.iroomclass.springbackend.domain.admin.exam.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.admin.exam.dto.ExamCreateRequest;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamCreateResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamDetailResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamListResponse;
import com.iroomclass.springbackend.domain.admin.exam.dto.ExamUpdateRequest;
import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.admin.exam.entity.ExamDraft;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamDraftRepository;
import com.iroomclass.springbackend.domain.admin.exam.repository.ExamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 실제 시험 관리 서비스
 * 
 * 시험 등록, 조회, 수정, 삭제 기능을 제공합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExamService {
    
    private final ExamRepository examRepository;
    private final ExamDraftRepository examDraftRepository;
    
    /**
     * 시험 등록
     * 
     * @param request 시험 등록 요청
     * @return 생성된 시험 정보
     */
    @Transactional
    public ExamCreateResponse createExam(ExamCreateRequest request) {
        log.info("시험 등록 요청: 시험지 초안 ID={}, 학생 수={}", request.getExamDraftId(), request.getStudentCount());
        
        // 1단계: 시험지 초안 조회
        ExamDraft examDraft = examDraftRepository.findById(request.getExamDraftId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지 초안입니다: " + request.getExamDraftId()));
        
        // 2단계: QR 코드 URL 생성
        String qrCodeUrl = generateQrCodeUrl(examDraft.getId());
        
        // 3단계: 시험 생성
        Exam exam = Exam.builder()
            .examDraft(examDraft)
            .examName(examDraft.getExamName())
            .grade(examDraft.getGrade())
            .content(request.getContent())
            .studentCount(request.getStudentCount())
            .qrCodeUrl(qrCodeUrl)
            .build();
        
        exam = examRepository.save(exam);
        
        log.info("시험 등록 완료: ID={}, 이름={}", exam.getId(), exam.getExamName());
        
        return ExamCreateResponse.builder()
            .examId(exam.getId())
            .examName(exam.getExamName())
            .grade(exam.getGrade())
            .studentCount(exam.getStudentCount())
            .qrCodeUrl(exam.getQrCodeUrl())
            .createdAt(exam.getCreatedAt())
            .build();
    }
    
    /**
     * 학년별 시험 목록 조회
     * 
     * @param grade 학년
     * @return 해당 학년의 시험 목록
     */
    public ExamListResponse getExamsByGrade(int grade) {
        log.info("학년별 시험 목록 조회 요청: {}학년", grade);
        
        List<Exam> exams = examRepository.findByGradeOrderByCreatedAtDesc(grade);
        
        List<ExamListResponse.ExamInfo> examInfos = new ArrayList<>();
        for (Exam exam : exams) {
            ExamListResponse.ExamInfo examInfo = ExamListResponse.ExamInfo.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .grade(exam.getGrade())
                .studentCount(exam.getStudentCount())
                .qrCodeUrl(exam.getQrCodeUrl())
                .createdAt(exam.getCreatedAt())
                .build();
            examInfos.add(examInfo);
        }
        
        log.info("학년별 시험 목록 조회 완료: {}학년, {}개", grade, examInfos.size());
        
        return ExamListResponse.builder()
            .grade(grade)
            .exams(examInfos)
            .totalCount(examInfos.size())
            .build();
    }
    
    /**
     * 전체 시험 목록 조회
     * 
     * @return 모든 시험 목록 (최신순)
     */
    public ExamListResponse getAllExams() {
        log.info("전체 시험 목록 조회 요청");
        
        List<Exam> exams = examRepository.findAllByOrderByCreatedAtDesc();
        
        List<ExamListResponse.ExamInfo> examInfos = new ArrayList<>();
        for (Exam exam : exams) {
            ExamListResponse.ExamInfo examInfo = ExamListResponse.ExamInfo.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .grade(exam.getGrade())
                .studentCount(exam.getStudentCount())
                .qrCodeUrl(exam.getQrCodeUrl())
                .createdAt(exam.getCreatedAt())
                .build();
            examInfos.add(examInfo);
        }
        
        log.info("전체 시험 목록 조회 완료: {}개", examInfos.size());
        
        return ExamListResponse.builder()
            .grade(null) // 전체 목록이므로 학년 정보 없음
            .exams(examInfos)
            .totalCount(examInfos.size())
            .build();
    }
    
    /**
     * 시험 상세 조회
     * 
     * @param examId 시험 ID
     * @return 시험 상세 정보
     */
    public ExamDetailResponse getExamDetail(Long examId) {
        log.info("시험 상세 조회 요청: ID={}", examId);
        
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));
        
        ExamDraft examDraft = exam.getExamDraft();
        
        log.info("시험 상세 조회 완료: ID={}, 이름={}", examId, exam.getExamName());
        
        return ExamDetailResponse.builder()
            .examId(exam.getId())
            .examDraftId(examDraft.getId())
            .examName(exam.getExamName())
            .grade(exam.getGrade())
            .content(exam.getContent())
            .studentCount(exam.getStudentCount())
            .qrCodeUrl(exam.getQrCodeUrl())
            .createdAt(exam.getCreatedAt())
            .build();
    }
    
    /**
     * 시험 수정
     * 
     * @param examId 시험 ID
     * @param request 수정 요청
     * @return 수정된 시험 정보
     */
    @Transactional
    public ExamDetailResponse updateExam(Long examId, ExamUpdateRequest request) {
        log.info("시험 수정 요청: ID={}", examId);
        
        // 1단계: 시험 조회
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));
        
        // 2단계: 시험 정보 수정
        exam.updateExamInfo(request.getExamName(), request.getContent(), request.getStudentCount());
        
        exam = examRepository.save(exam);
        
        log.info("시험 수정 완료: ID={}", examId);
        
        // 3단계: 수정된 시험 상세 정보 반환
        return getExamDetail(examId);
    }
    
    /**
     * 시험 삭제
     * 
     * @param examId 시험 ID
     */
    @Transactional
    public void deleteExam(Long examId) {
        log.info("시험 삭제 요청: ID={}", examId);
        
        // 1단계: 시험 존재 확인
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));
        
        // 2단계: 시험 삭제
        examRepository.delete(exam);
        
        log.info("시험 삭제 완료: ID={}", examId);
    }
    
    /**
     * QR 코드 URL 생성
     */
    private String generateQrCodeUrl(Long examDraftId) {
        // 실제로는 QR 코드 이미지를 생성하고 저장해야 하지만,
        // 여기서는 임시 URL을 반환합니다.
        return "https://example.com/exam/qr/" + examDraftId + "/" + UUID.randomUUID().toString();
    }
}
