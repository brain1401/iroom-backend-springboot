package com.iroomclass.springbackend.domain.user.exam.answer.repository;

import com.iroomclass.springbackend.domain.user.exam.answer.entity.StudentAnswerSheet;
import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 학생 답안지 Repository
 * 
 * 학생 답안지 데이터의 CRUD 작업을 담당합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentAnswerSheetRepository extends JpaRepository<StudentAnswerSheet, UUID> {

    /**
     * 시험 제출별 답안 목록 조회
     * 
     * 사용처: 학생이 제출한 모든 답안 조회
     * 예시: 특정 시험 제출의 모든 문제 답안 조회
     * 
     * @param examSubmission 시험 제출
     * @return 해당 시험 제출의 답안 목록
     */
    List<StudentAnswerSheet> findByExamSubmission(ExamSubmission examSubmission);

    /**
     * 시험 제출 ID로 답안 목록 조회
     * 
     * 사용처: 시험 제출 ID로 해당 제출의 모든 답안 조회
     * 예시: 시험 제출 ID로 모든 문제 답안 조회
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 답안 목록
     */
    List<StudentAnswerSheet> findByExamSubmissionId(UUID examSubmissionId);

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
    @Query("SELECT sas FROM StudentAnswerSheet sas WHERE sas.examSubmission.id = :examSubmissionId AND sas.question.id = :questionId")
    Optional<StudentAnswerSheet> findByExamSubmissionIdAndQuestionId(@Param("examSubmissionId") UUID examSubmissionId, @Param("questionId") UUID questionId);

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
     * 정답 개수 조회
     * 
     * 사용처: 정답 개수 확인
     * 예시: 채점된 정답 개수 확인
     * 
     * @param examSubmissionId 시험 제출 ID
     * @return 해당 시험 제출의 정답 수
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
    @Query("SELECT COUNT(sas) > 0 FROM StudentAnswerSheet sas WHERE sas.examSubmission.id = :examSubmissionId AND sas.question.id = :questionId")
    boolean existsByExamSubmissionIdAndQuestionId(@Param("examSubmissionId") UUID examSubmissionId, @Param("questionId") UUID questionId);

    /**
     * 여러 시험 제출 ID로 답안 목록 조회
     * 
     * 사용처: 여러 시험 제출의 답안을 한 번에 조회
     * 예시: 특정 학년의 모든 시험 제출 답안 조회
     * 
     * @param examSubmissionIds 시험 제출 ID 목록
     * @return 해당 시험 제출들의 답안 목록
     */
    List<StudentAnswerSheet> findByExamSubmissionIdIn(List<UUID> examSubmissionIds);

    /**
     * 시험 제출 ID로 답안 목록을 문제 순서대로 조회
     * 
     * 사용처: 시험 제출의 답안을 문제 순서대로 정렬하여 조회
     * 예시: 채점 시 문제 순서대로 답안 처리
     * 
     * @param submissionId 시험 제출 ID
     * @return 문제 순서대로 정렬된 답안 목록
     */
    @Query("SELECT sas FROM StudentAnswerSheet sas JOIN sas.question q WHERE sas.examSubmission.id = :submissionId ORDER BY q.questionOrder ASC")
    List<StudentAnswerSheet> findByExamSubmissionIdOrderByQuestionOrder(@Param("submissionId") UUID submissionId);

    /**
     * 시험 제출 ID로 답안 목록을 문제 순서대로 조회 (Question 정보 포함)
     * 
     * 사용처: 채점 시 문제 정보와 함께 답안 조회
     * 예시: 자동 채점 시 Question 정보가 필요한 경우
     * 
     * @param submissionId 시험 제출 ID
     * @return 문제 순서대로 정렬된 답안 목록 (Question 정보 포함)
     */
    @Query("SELECT sas FROM StudentAnswerSheet sas JOIN FETCH sas.question q WHERE sas.examSubmission.id = :submissionId ORDER BY q.questionOrder ASC")
    List<StudentAnswerSheet> findBySubmissionIdOrderByQuestionOrderWithQuestion(@Param("submissionId") UUID submissionId);

    /**
     * 시험 제출 ID로 답안 목록을 문제 순서대로 조회 (별칭)
     * 
     * @param submissionId 시험 제출 ID
     * @return 문제 순서대로 정렬된 답안 목록
     */
    default List<StudentAnswerSheet> findBySubmissionIdOrderByQuestionOrder(UUID submissionId) {
        return findByExamSubmissionIdOrderByQuestionOrder(submissionId);
    }
}