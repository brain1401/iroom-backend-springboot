package com.iroomclass.springbackend.domain.exam.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheetProblem;

/**
 * 학생 답안지 문제별 답안 Repository
 * 
 * 학생의 문제별 답안 데이터를 관리합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentAnswerSheetProblemRepository extends JpaRepository<StudentAnswerSheetProblem, UUID> {

    /**
     * 특정 답안지의 모든 문제별 답안 조회
     * 
     * @param studentAnswerSheetId 학생 답안지 ID
     * @return 문제별 답안 목록
     */
    List<StudentAnswerSheetProblem> findByStudentAnswerSheet_Id(UUID studentAnswerSheetId);

    /**
     * 특정 문제의 모든 답안 조회
     * 
     * @param questionId 문제 ID
     * @return 문제별 답안 목록
     */
    List<StudentAnswerSheetProblem> findByQuestion_Id(UUID questionId);

    /**
     * 특정 답안지의 특정 문제 답안 조회
     * 
     * @param studentAnswerSheetId 학생 답안지 ID
     * @param questionId           문제 ID
     * @return 해당 문제의 답안
     */
    @Query("SELECT p FROM StudentAnswerSheetProblem p " +
           "WHERE p.studentAnswerSheet.id = :studentAnswerSheetId " +
           "AND p.question.id = :questionId")
    StudentAnswerSheetProblem findByStudentAnswerSheetIdAndQuestionId(
            @Param("studentAnswerSheetId") UUID studentAnswerSheetId,
            @Param("questionId") UUID questionId);

    /**
     * 특정 답안지의 답안이 있는 문제 수 조회
     * 
     * @param studentAnswerSheetId 학생 답안지 ID
     * @return 답안이 있는 문제 수
     */
    @Query("SELECT COUNT(p) FROM StudentAnswerSheetProblem p " +
           "WHERE p.studentAnswerSheet.id = :studentAnswerSheetId " +
           "AND (p.answerText IS NOT NULL OR p.selectedChoice IS NOT NULL)")
    int countAnsweredProblemsByStudentAnswerSheetId(@Param("studentAnswerSheetId") UUID studentAnswerSheetId);

    /**
     * 특정 답안지의 전체 문제 수 조회
     * 
     * @param studentAnswerSheetId 학생 답안지 ID
     * @return 전체 문제 수
     */
    int countByStudentAnswerSheet_Id(UUID studentAnswerSheetId);

    /**
     * 특정 제출의 모든 문제별 답안을 문제와 함께 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 문제별 답안 목록 (문제 정보 포함)
     */
    @Query("SELECT p FROM StudentAnswerSheetProblem p " +
           "JOIN FETCH p.question q " +
           "WHERE p.studentAnswerSheet.examSubmission.id = :submissionId " +
           "ORDER BY q.id")
    List<StudentAnswerSheetProblem> findBySubmissionIdWithQuestions(@Param("submissionId") UUID submissionId);

    /**
     * 답안이 있는 문제별 답안들만 조회
     * 
     * @param studentAnswerSheetId 학생 답안지 ID
     * @return 답안이 있는 문제별 답안 목록
     */
    @Query("SELECT p FROM StudentAnswerSheetProblem p " +
           "WHERE p.studentAnswerSheet.id = :studentAnswerSheetId " +
           "AND (p.answerText IS NOT NULL OR p.selectedChoice IS NOT NULL)")
    List<StudentAnswerSheetProblem> findAnsweredProblemsByStudentAnswerSheetId(@Param("studentAnswerSheetId") UUID studentAnswerSheetId);
}