package com.iroomclass.springbackend.domain.exam.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.submission.ExamSubmissionCreateResponse;
import com.iroomclass.springbackend.domain.exam.entity.Exam;
import com.iroomclass.springbackend.domain.exam.repository.ExamRepository;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.user.entity.User;
import com.iroomclass.springbackend.domain.user.repository.StudentRepository;

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
    private final StudentAnswerSheetRepository studentAnswerSheetRepository;
    private final StudentRepository userRepository;

    /**
     * 시험 제출 생성
     * 
     * @param request 시험 제출 생성 요청
     * @return 생성된 시험 제출 정보
     */
    @Transactional
    public ExamSubmissionCreateResponse createExamSubmission(ExamSubmissionCreateRequest request) {
        log.info("시험 제출 생성 요청: 시험={}, 학생={}, 전화번호={}",
                request.examId(), request.studentName(), request.studentPhone());

        // 1단계: 시험 존재 확인
        Exam exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다: " + request.examId()));

        // 2단계: 중복 제출 방지
        if (examSubmissionRepository.existsByExamIdAndUserNameAndUserPhone(
                request.examId(), request.studentName(), request.studentPhone())) {
            throw new IllegalArgumentException("이미 제출한 시험입니다.");
        }

        // 3단계: 학생 정보 확인 및 등록
        User user = userRepository.findByNameAndPhone(request.studentName(), request.studentPhone())
                .orElseGet(() -> {
                    log.info("새로운 학생 등록: 이름={}, 전화번호={}", request.studentName(), request.studentPhone());
                    User newUser = User.builder()
                            .name(request.studentName())
                            .phone(request.studentPhone())
                            .build();
                    return userRepository.save(newUser);
                });

        // 4단계: 시험 제출 생성
        ExamSubmission submission = ExamSubmission.builder()
                .exam(exam)
                .user(user)
                .build();

        submission = examSubmissionRepository.save(submission);

        log.info("시험 제출 생성 완료: ID={}, 학생={}, 시험={}, 사용자ID={}",
                submission.getId(), request.studentName(), exam.getExamName(), user.getId());

        return new ExamSubmissionCreateResponse(
                submission.getId(),
                exam.getId(),
                exam.getExamName(),
                submission.getUser().getName(),
                submission.getUser().getPhone(),
                submission.getSubmittedAt(),
                exam.getQrCodeUrl());
    }

    /**
     * 시험 최종 제출
     * 
     * @param submissionId 시험 제출 ID
     * @return 최종 제출 완료 정보
     */
    @Transactional
    public ExamSubmissionCreateResponse finalSubmitExam(UUID submissionId) {
        log.info("시험 최종 제출 요청: 제출 ID={}", submissionId);

        // 1단계: 시험 제출 존재 확인
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험 제출입니다: " + submissionId));

        // 2단계: 답안 완료 여부 확인
        long answerCount = studentAnswerSheetRepository.countByExamSubmissionId(submissionId);
        if (answerCount == 0) {
            throw new IllegalArgumentException("답안이 완료되지 않았습니다. 답안을 먼저 작성해주세요.");
        }

        // 3단계: 최종 제출 처리 (submittedAt 업데이트)
        submission.updateSubmittedAt();
        submission = examSubmissionRepository.save(submission);

        log.info("시험 최종 제출 완료: 제출 ID={}, 학생={}, 답안 수={}",
                submission.getId(), submission.getUser().getName(), answerCount);

        return new ExamSubmissionCreateResponse(
                submission.getId(),
                submission.getExam().getId(),
                submission.getExam().getExamName(),
                submission.getUser().getName(),
                submission.getUser().getPhone(),
                submission.getSubmittedAt(),
                submission.getExam().getQrCodeUrl());
    }
}
