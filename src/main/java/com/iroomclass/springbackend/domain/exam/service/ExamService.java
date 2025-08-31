package com.iroomclass.springbackend.domain.exam.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.dto.exam.ExamCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamCreateResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamDetailResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamListResponse;
import com.iroomclass.springbackend.domain.exam.dto.exam.ExamUpdateRequest;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheet;
import com.iroomclass.springbackend.domain.exam.entity.ExamDocument;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.ExamDocumentRepository;

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
    private final ExamSheetRepository examSheetRepository;
    private final ExamDocumentRepository examDocumentRepository;

    /**
     * 시험 등록
     * 
     * @param request 시험 등록 요청
     * @return 생성된 시험 정보
     */
    @Transactional
    public ExamCreateResponse createExam(ExamCreateRequest request) {
        log.info("시험 등록 요청: 시험지 ID={}, 학생 수={}", request.examSheetId(), request.studentCount());

        // 1단계: 시험지 조회
        ExamSheet examSheet = examSheetRepository.findById(request.examSheetId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험지입니다: " + request.examSheetId()));

        // 2단계: QR 코드 URL 생성
        String qrCodeUrl = generateQrCodeUrl(examSheet.getId());

        // 3단계: 시험 생성
        Exam exam = Exam.builder()
                .examSheet(examSheet)
                .examName(examSheet.getExamName())
                .grade(examSheet.getGrade())
                .content(request.content())
                .studentCount(request.studentCount())
                .qrCodeUrl(qrCodeUrl)
                .build();

        exam = examRepository.save(exam);

        log.info("시험 등록 완료: ID={}, 이름={}", exam.getId(), exam.getExamName());

        return new ExamCreateResponse(
                exam.getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getStudentCount(),
                exam.getQrCodeUrl(),
                exam.getCreatedAt());
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
            ExamListResponse.ExamInfo examInfo = new ExamListResponse.ExamInfo(
                    exam.getId(),
                    exam.getExamName(),
                    exam.getGrade(),
                    exam.getStudentCount(),
                    exam.getQrCodeUrl(),
                    exam.getCreatedAt());
            examInfos.add(examInfo);
        }

        log.info("학년별 시험 목록 조회 완료: {}학년, {}개", grade, examInfos.size());

        return new ExamListResponse(
                grade,
                examInfos,
                examInfos.size());
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
            ExamListResponse.ExamInfo examInfo = new ExamListResponse.ExamInfo(
                    exam.getId(),
                    exam.getExamName(),
                    exam.getGrade(),
                    exam.getStudentCount(),
                    exam.getQrCodeUrl(),
                    exam.getCreatedAt());
            examInfos.add(examInfo);
        }

        log.info("전체 시험 목록 조회 완료: {}개", examInfos.size());

        return new ExamListResponse(
                null, // 전체 목록이므로 학년 정보 없음
                examInfos,
                examInfos.size());
    }

    /**
     * 시험 상세 조회
     * 
     * @param examId 시험 ID
     * @return 시험 상세 정보
     */
    public ExamDetailResponse getExamDetail(UUID examId) {
        log.info("시험 상세 조회 요청: ID={}", examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));

        log.info("시험 상세 조회 완료: ID={}, 이름={}", examId, exam.getExamName());

        return new ExamDetailResponse(
                exam.getId(),
                exam.getExamSheet().getId(),
                exam.getExamName(),
                exam.getGrade(),
                exam.getContent(),
                exam.getStudentCount(),
                exam.getQrCodeUrl(),
                exam.getCreatedAt());
    }

    /**
     * 시험 수정
     * 
     * @param examId  시험 ID
     * @param request 수정 요청
     * @return 수정된 시험 정보
     */
    @Transactional
    public ExamDetailResponse updateExam(UUID examId, ExamUpdateRequest request) {
        log.info("시험 수정 요청: ID={}", examId);

        // 1단계: 시험 조회
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + examId));

        // 2단계: 시험 정보 수정
        exam.updateExamInfo(request.examName(), request.content(), request.studentCount());

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
    public void deleteExam(UUID examId) {
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
    private String generateQrCodeUrl(UUID examSheetId) {
        // 답안지에서 QR 코드 URL 가져오기
        List<ExamDocument> documents = examDocumentRepository.findByExamSheetId(examSheetId);

        // ANSWER_SHEET 타입의 문서 찾기
        ExamDocument answerSheet = documents.stream()
                .filter(doc -> doc.getDocumentType() == ExamDocument.DocumentType.STUDENT_ANSWER_SHEET)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 시험지에 대한 답안지가 존재하지 않습니다: " + examSheetId));

        String qrCodeUrl = answerSheet.getQrCodeUrl();
        if (qrCodeUrl == null || qrCodeUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("답안지에 QR 코드가 생성되지 않았습니다. 먼저 시험지 문서를 생성해주세요.");
        }

        log.info("답안지에서 QR 코드 URL 가져오기: examSheetId={}, qrCodeUrl={}", examSheetId, qrCodeUrl);
        return qrCodeUrl;
    }
}
