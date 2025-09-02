package com.iroomclass.springbackend.domain.exam.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult.ResultStatus;

/**
 * AI 시험 결과 Repository
 * 
 * AI 기반 시험 채점 결과에 대한 데이터 접근 계층을 제공합니다.
 * AI 재채점 히스토리, 상태별 필터링, 점수별 조회 등을 지원합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {
    
    /**
     * 제출 ID로 모든 채점 결과 조회 (재채점 히스토리 포함)
     * 
     * @param submissionId 시험 제출 ID
     * @return 채점 결과 목록 (버전 내림차순)
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.examSubmission.id = :submissionId " +
           "ORDER BY er.version DESC")
    List<ExamResult> findAllBySubmissionIdOrderByVersionDesc(@Param("submissionId") UUID submissionId);
    
    /**
     * 제출 ID로 최신 채점 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @return 최신 채점 결과
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.examSubmission.id = :submissionId " +
           "ORDER BY er.version DESC " +
           "LIMIT 1")
    Optional<ExamResult> findLatestBySubmissionId(@Param("submissionId") UUID submissionId);
    
    // AI 자동 채점 시스템으로 인해 채점자별 조회는 더 이상 필요하지 않음
    
    /**
     * AI 자동 채점 결과 조회 (모든 채점 결과)
     * 
     * @param pageable 페이징 정보
     * @return AI 자동 채점 결과 페이지
     */
    @Query("SELECT er FROM ExamResult er " +
           "ORDER BY er.gradedAt DESC")
    Page<ExamResult> findAutoGradedResults(Pageable pageable);
    
    /**
     * 채점 상태별 결과 조회
     * 
     * @param status 채점 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 채점 결과 페이지
     */
    Page<ExamResult> findByStatusOrderByGradedAtDesc(ResultStatus status, Pageable pageable);
    
    /**
     * 특정 기간 내 채점 결과 조회
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @param pageable 페이징 정보
     * @return 기간 내 채점 결과 페이지
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.gradedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY er.gradedAt DESC")
    Page<ExamResult> findByGradedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * 재채점된 결과만 조회
     * 
     * @param pageable 페이징 정보
     * @return 재채점 결과 페이지
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.version > 1 OR er.status = 'REGRADED' " +
           "ORDER BY er.gradedAt DESC")
    Page<ExamResult> findRegradedResults(Pageable pageable);
    
    /**
     * 특정 점수 범위의 결과 조회
     * 
     * @param minScore 최소 점수
     * @param maxScore 최대 점수
     * @param pageable 페이징 정보
     * @return 점수 범위 내 채점 결과 페이지
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.totalScore BETWEEN :minScore AND :maxScore " +
           "ORDER BY er.totalScore DESC")
    Page<ExamResult> findByTotalScoreBetween(
        @Param("minScore") Integer minScore,
        @Param("maxScore") Integer maxScore,
        Pageable pageable
    );
    
    /**
     * 제출 ID와 버전으로 특정 채점 결과 조회
     * 
     * @param submissionId 시험 제출 ID
     * @param version 채점 버전
     * @return 해당 버전의 채점 결과
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.examSubmission.id = :submissionId AND er.version = :version")
    Optional<ExamResult> findBySubmissionIdAndVersion(
        @Param("submissionId") UUID submissionId,
        @Param("version") Integer version
    );
    
    /**
     * 채점 상태별 개수 조회
     * 
     * @param status 채점 상태
     * @return 해당 상태의 채점 결과 개수
     */
    long countByStatus(ResultStatus status);
    
    // AI 자동 채점 시스템으로 인해 채점자별 개수 조회는 더 이상 필요하지 않음
    
    /**
     * AI 자동 채점 개수 조회 (전체 채점 개수)
     * 
     * @return AI 자동 채점 개수
     */
    @Query("SELECT COUNT(er) FROM ExamResult er")
    long countAutoGradedResults();
    
    /**
     * 제출물에 대한 채점 이력 존재 여부 확인
     * 
     * @param submissionId 시험 제출 ID
     * @return 채점 이력이 있으면 true
     */
    boolean existsByExamSubmissionId(UUID submissionId);
    
    /**
     * 완료된 채점 결과만 조회 (문제별 결과 포함)
     * 
     * @param pageable 페이징 정보
     * @return 완료된 채점 결과 페이지
     */
    @Query("SELECT DISTINCT er FROM ExamResult er " +
           "LEFT JOIN FETCH er.questionResults " +
           "WHERE er.status = 'COMPLETED' " +
           "ORDER BY er.gradedAt DESC")
    Page<ExamResult> findCompletedResultsWithQuestionResults(Pageable pageable);
    
    /**
     * 여러 제출 ID들에 대한 최신 채점 결과 일괄 조회 (N+1 문제 해결)
     * 
     * @param submissionIds 시험 제출 ID 목록
     * @return 각 제출의 최신 채점 결과 목록
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.examSubmission.id IN :submissionIds " +
           "AND er.version = (SELECT MAX(er2.version) FROM ExamResult er2 " +
           "                 WHERE er2.examSubmission.id = er.examSubmission.id)")
    List<ExamResult> findLatestBySubmissionIds(@Param("submissionIds") List<UUID> submissionIds);
    
    /**
     * 학년별 학생 평균 성적 통계 조회 (한 번의 쿼리로 처리)
     * 
     * @param grade 학년 (1, 2, 3)
     * @return 학생별 평균 성적 목록 (Student ID, 평균 점수)
     */
    @Query("SELECT s.id as studentId, s.name as studentName, AVG(er.totalScore) as averageScore " +
           "FROM ExamResult er " +
           "JOIN er.examSubmission es " +
           "JOIN es.student s " +
           "JOIN es.exam e " +
           "WHERE e.grade = :grade " +
           "AND er.version = (SELECT MAX(er2.version) FROM ExamResult er2 " +
           "                 WHERE er2.examSubmission.id = er.examSubmission.id) " +
           "GROUP BY s.id, s.name")
    List<StudentAverageScoreProjection> findStudentAverageScoresByGrade(@Param("grade") Integer grade);
    
    /**
     * 전체 학생 평균 성적 통계 조회 (한 번의 쿼리로 처리)
     * 
     * @return 학생별 평균 성적 목록 (Student ID, 평균 점수, 학년)
     */
    @Query("SELECT s.id as studentId, s.name as studentName, AVG(er.totalScore) as averageScore, e.grade as grade " +
           "FROM ExamResult er " +
           "JOIN er.examSubmission es " +
           "JOIN es.student s " +
           "JOIN es.exam e " +
           "WHERE er.version = (SELECT MAX(er2.version) FROM ExamResult er2 " +
           "                 WHERE er2.examSubmission.id = er.examSubmission.id) " +
           "GROUP BY s.id, s.name, e.grade")
    List<StudentAverageScoreWithGradeProjection> findAllStudentAverageScores();
    
    /**
     * 학생 평균 성적 Projection 인터페이스
     */
    interface StudentAverageScoreProjection {
        Long getStudentId();
        String getStudentName();
        Double getAverageScore();
    }
    
    /**
     * 학년별 학생 평균 성적 Projection 인터페이스
     */
    interface StudentAverageScoreWithGradeProjection {
        Long getStudentId();
        String getStudentName();
        Double getAverageScore();
        Integer getGrade();
    }
}