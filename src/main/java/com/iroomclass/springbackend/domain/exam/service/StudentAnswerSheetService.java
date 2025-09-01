package com.iroomclass.springbackend.domain.exam.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.exam.repository.StudentAnswerSheetRepository;
import com.iroomclass.springbackend.domain.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.exam.repository.ExamSubmissionRepository;
import com.iroomclass.springbackend.domain.exam.entity.Question;
import com.iroomclass.springbackend.domain.exam.repository.QuestionRepository;
import com.iroomclass.springbackend.domain.exam.dto.answer.ExamAnswerListResponse;
import com.iroomclass.springbackend.domain.exam.dto.answer.StudentExamAnswerResponse;
import com.iroomclass.springbackend.domain.exam.dto.answer.ExamAnswerSheetCreateRequest;
import com.iroomclass.springbackend.domain.exam.dto.answer.ExamAnswerSheetProcessResponse;
import com.iroomclass.springbackend.domain.exam.dto.answer.ExamAnswerUpdateRequest;
import com.iroomclass.springbackend.domain.exam.dto.answer.RecognizedAnswer;
import com.iroomclass.springbackend.domain.exam.entity.ExamSheetQuestion;
import com.iroomclass.springbackend.domain.exam.repository.ExamSheetQuestionRepository;
import com.iroomclass.springbackend.domain.exam.service.QuestionResultService;
import com.iroomclass.springbackend.domain.exam.entity.QuestionResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학생 답안지 관리 서비스
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
public class StudentAnswerSheetService {

    private final StudentAnswerSheetRepository studentAnswerSheetRepository;

    /**
     * 답안 목록 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 모든 답안 목록
     */
    public ExamAnswerListResponse getStudentAnswers(UUID examSubmissionId) {
        log.info("답안 목록 조회 요청: 시험 제출 ID={}", examSubmissionId);

        List<StudentAnswerSheet> studentAnswerSheets = studentAnswerSheetRepository
                .findByExamSubmissionId(examSubmissionId);

        log.info("답안 목록 조회 완료: 시험 제출 ID={}, 답안 수={}", examSubmissionId, studentAnswerSheets.size());

        return ExamAnswerListResponse.from(studentAnswerSheets, examSubmissionId);
    }

    /**
     * 특정 문제 답안 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @param questionId       문제 ID
     * @return 해당 문제의 답안 정보
     */
    public StudentExamAnswerResponse getStudentAnswer(UUID examSubmissionId, UUID questionId) {
        log.info("특정 문제 답안 조회 요청: 시험 제출 ID={}, 문제 ID={}", examSubmissionId, questionId);

        StudentAnswerSheet studentAnswerSheet = studentAnswerSheetRepository
                .findByExamSubmissionIdAndQuestionId(examSubmissionId, questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 답안입니다: 시험 제출 ID=" + examSubmissionId + ", 문제 ID=" + questionId));

        log.info("특정 문제 답안 조회 완료: 답안 ID={}", studentAnswerSheet.getId());

        return StudentExamAnswerResponse.from(studentAnswerSheet);
    }

    /**
     * 답안 상태 요약
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 답안 상태 요약 정보
     */
    public AnswerStatusSummary getAnswerStatusSummary(UUID examSubmissionId) {
        log.info("답안 상태 확인 요청: 시험 제출 ID={}", examSubmissionId);

        long totalCount = studentAnswerSheetRepository.countByExamSubmissionId(examSubmissionId);
        long correctCount = studentAnswerSheetRepository.countByExamSubmissionIdAndIsCorrectTrue(examSubmissionId);

        AnswerStatusSummary summary = AnswerStatusSummary.builder()
                .totalCount((int) totalCount)
                .correctCount((int) correctCount)
                .build();

        log.info("답안 상태 확인 완료: 총 {}개, 정답 {}개", totalCount, correctCount);

        return summary;
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

        public int getTotalCount() {
            return totalCount;
        }

        public int getCorrectCount() {
            return correctCount;
        }

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