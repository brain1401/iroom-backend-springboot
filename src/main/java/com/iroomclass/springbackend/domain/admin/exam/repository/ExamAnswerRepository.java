package com.iroomclass.springbackend.domain.admin.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iroomclass.springbackend.domain.admin.exam.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;

/**
 * 시험 답안 Repository
 * 
 * 시험 답안 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    
    /**
     * 시험 제출별 답안 목록 조회
     * 
     * 사용처: 답안 보기에서 학생의 모든 문제 답안 조회
     * 예시: "김철수"의 "1학년 중간고사" → 1번부터 20번까지의 답안들 조회
     * 
     * @param submission 시험 제출
     * @return 해당 제출의 답안 목록
     */
    List<ExamAnswer> findBySubmission(ExamSubmission submission);

    /**
     * 시험 제출 ID로 답안 목록 조회
     * 
     * 사용처: 답안 보기에서 학생의 모든 문제 답안 조회 (ID로 조회)
     * 예시: 제출 ID로 해당 학생의 모든 답안 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 해당 제출의 답안 목록
     */
    List<ExamAnswer> findBySubmissionId(Long submissionId);

    /**
     * 정답 여부별 답안 조회
     * 
     * 사용처: 정답/오답 통계, 성적 분석
     * 예시: "김철수"의 정답들만 조회, 오답들만 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param isCorrect 정답 여부 (true: 정답, false: 오답)
     * @return 해당 조건의 답안 목록
     */
    List<ExamAnswer> findBySubmissionIdAndIsCorrect(Long submissionId, Boolean isCorrect);
}