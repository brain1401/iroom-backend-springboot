package com.iroomclass.springbackend.domain.exam.repository;

import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 학생 답안지 Repository
 * 
 * <p>학생 답안지 데이터 조회 및 관리를 위한 Repository입니다.
 * 시험 제출과 연관된 답안 정보를 조회하는 기능을 제공합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface StudentAnswerSheetRepository extends JpaRepository<StudentAnswerSheet, UUID> {
    
    /**
     * 시험 제출 ID로 학생 답안지 조회 (문제별 답안 포함)
     * 
     * @param submissionId 시험 제출 ID
     * @return 학생 답안지 (문제별 답안 포함)
     */
    @Query("SELECT sas FROM StudentAnswerSheet sas " +
           "LEFT JOIN FETCH sas.studentAnswerSheetQuestions sasq " +
           "LEFT JOIN FETCH sasq.question q " +
           "LEFT JOIN FETCH q.unit u " +
           "WHERE sas.examSubmission.id = :submissionId")
    Optional<StudentAnswerSheet> findBySubmissionIdWithQuestions(@Param("submissionId") UUID submissionId);
    
    /**
     * 시험 제출 ID로 학생 답안지 조회 (기본 정보만)
     * 
     * @param submissionId 시험 제출 ID
     * @return 학생 답안지 (기본 정보)
     */
    @Query("SELECT sas FROM StudentAnswerSheet sas " +
           "WHERE sas.examSubmission.id = :submissionId")
    Optional<StudentAnswerSheet> findBySubmissionId(@Param("submissionId") UUID submissionId);
    
    /**
     * 시험 제출 ID로 답안지 존재 여부 확인
     * 
     * @param submissionId 시험 제출 ID
     * @return 답안지 존재 여부
     */
    @Query("SELECT COUNT(sas) > 0 FROM StudentAnswerSheet sas " +
           "WHERE sas.examSubmission.id = :submissionId")
    boolean existsBySubmissionId(@Param("submissionId") UUID submissionId);
    
    /**
     * 시험 제출 ID와 학생 이름으로 답안지 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param studentName 학생 이름
     * @return 학생 답안지
     */
    @Query("SELECT sas FROM StudentAnswerSheet sas " +
           "LEFT JOIN FETCH sas.studentAnswerSheetQuestions " +
           "WHERE sas.examSubmission.id = :submissionId " +
           "AND sas.studentName = :studentName")
    Optional<StudentAnswerSheet> findBySubmissionIdAndStudentName(
            @Param("submissionId") UUID submissionId, 
            @Param("studentName") String studentName);
    
    /**
     * 시험 ID로 모든 학생 답안지 수 조회
     * 
     * @param examId 시험 ID
     * @return 답안지 수
     */
    @Query("SELECT COUNT(sas) FROM StudentAnswerSheet sas " +
           "JOIN sas.examSubmission es " +
           "WHERE es.exam.id = :examId")
    long countByExamId(@Param("examId") UUID examId);
}