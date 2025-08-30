package com.iroomclass.springbackend.domain.user.exam.result.repository;

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

import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult.ResultStatus;

/**
 * 시험 결과 Repository
 * 
 * 시험 채점 결과에 대한 데이터 접근 계층을 제공합니다.
 * 재채점 히스토리, 채점자별 조회, 상태별 필터링 등을 지원합니다.
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
    
    /**
     * 채점자별 채점 결과 조회
     * 
     * @param graderId 채점자 ID
     * @param pageable 페이징 정보
     * @return 채점 결과 페이지
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.gradedBy.id = :graderId " +
           "ORDER BY er.gradedAt DESC")
    Page<ExamResult> findByGraderId(@Param("graderId") UUID graderId, Pageable pageable);
    
    /**
     * 자동 채점 결과 조회
     * 
     * @param pageable 페이징 정보
     * @return 자동 채점 결과 페이지
     */
    @Query("SELECT er FROM ExamResult er " +
           "WHERE er.gradedBy IS NULL " +
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
    
    /**
     * 특정 채점자의 채점 개수 조회
     * 
     * @param graderId 채점자 ID
     * @return 채점 개수
     */
    @Query("SELECT COUNT(er) FROM ExamResult er WHERE er.gradedBy.id = :graderId")
    long countByGraderId(@Param("graderId") UUID graderId);
    
    /**
     * 자동 채점 개수 조회
     * 
     * @return 자동 채점 개수
     */
    @Query("SELECT COUNT(er) FROM ExamResult er WHERE er.gradedBy IS NULL")
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
}