package com.iroomclass.springbackend.domain.user.exam.answer.repository;

import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 시험 답안 Repository
 * 
 * 시험 답안 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, UUID> {

    /**
     * 시험 제출별 답안 목록 조회
     * 
     * 사용처: 학생이 제출한 모든 답안 조회
     * 예시: 특정 시험 제출의 모든 문제 답안 조회
     * 
     * @param examSubmission 시험 제출
     * @return 해당 시험 제출의 답안 목록
     */
    List<ExamAnswer> findByExamSubmission(ExamSubmission examSubmission);

    /**
     * 시험 제출 ID로 답안 목록 조회
     * 
     * 사용처: 시험 제출 ID로 해당 제출의 모든 답안 조회
     * 예시: 시험 제출 ID로 모든 문제 답안 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 답안 목록
     */
    List<ExamAnswer> findByExamSubmissionId(UUID examSubmissionId);

    /**
     * 특정 문제의 답안 조회
     * 
     * 사용처: 특정 문제의 답안만 조회
     * 예시: 특정 문제의 답안만 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @param questionId       문제 ID
     * @return 해당 문제의 답안
     */
    Optional<ExamAnswer> findByExamSubmissionIdAndQuestionId(UUID examSubmissionId, UUID questionId);

    /**
     * 시험 제출별 답안 수 조회
     * 
     * 사용처: 제출한 답안 개수 확인
     * 예시: 총 몇 개의 문제에 답안을 제출했는지 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 답안 수
     */
    long countByExamSubmissionId(UUID examSubmissionId);

    /**
     * 정답 답안 수 조회
     * 
     * 사용처: 정답인 답안 개수 확인
     * 예시: 정답인 답안이 몇 개인지 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 정답인 답안 수
     */
    long countByExamSubmissionIdAndIsCorrectTrue(UUID examSubmissionId);

    /**
     * 답안 존재 여부 확인
     * 
     * 사용처: 특정 문제에 답안이 있는지 확인
     * 예시: 특정 문제에 답안이 이미 있는지 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @param questionId       문제 ID
     * @return 답안 존재 여부
     */
    boolean existsByExamSubmissionIdAndQuestionId(UUID examSubmissionId, UUID questionId);

    /**
     * 여러 시험 제출 ID로 답안 목록 조회
     * 
     * 사용처: 여러 시험 제출의 답안을 한 번에 조회
     * 예시: 특정 학년의 모든 시험 제출 답안 조회
     * 
     * @param examSubmissionIds 시험 제출 ID 목록
     * @return 해당 시험 제출들의 답안 목록
     */
    List<ExamAnswer> findByExamSubmissionIdIn(List<UUID> examSubmissionIds);
}
