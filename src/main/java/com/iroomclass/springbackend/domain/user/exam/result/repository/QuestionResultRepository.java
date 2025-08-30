package com.iroomclass.springbackend.domain.user.exam.result.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult.GradingMethod;

/**
 * 문제별 채점 결과 Repository
 * 
 * 문제별 채점 결과에 대한 데이터 접근 계층을 제공합니다.
 * 채점 방법별 조회, 정답률 통계, AI 신뢰도 분석 등을 지원합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Repository
public interface QuestionResultRepository extends JpaRepository<QuestionResult, UUID> {
    
    /**
     * 시험 결과 ID로 문제별 결과 조회
     * 
     * @param examResultId 시험 결과 ID
     * @return 문제별 결과 목록 (문제 순서대로)
     */
    @Query("SELECT qr FROM QuestionResult qr " +
           "LEFT JOIN FETCH qr.examAnswer " +
           "WHERE qr.examResult.id = :examResultId " +
           "ORDER BY qr.examAnswer.questionOrder ASC")
    List<QuestionResult> findByExamResultIdOrderByQuestionOrder(@Param("examResultId") UUID examResultId);
    
    /**
     * 특정 문제에 대한 모든 채점 결과 조회
     * 
     * @param questionId 문제 ID
     * @param pageable 페이징 정보
     * @return 해당 문제의 채점 결과 페이지
     */
    @Query("SELECT qr FROM QuestionResult qr " +
           "JOIN qr.examAnswer ea " +
           "WHERE ea.questionId = :questionId " +
           "ORDER BY qr.createdAt DESC")
    Page<QuestionResult> findByQuestionId(@Param("questionId") UUID questionId, Pageable pageable);
    
    /**
     * 채점 방법별 결과 조회
     * 
     * @param gradingMethod 채점 방법
     * @param pageable 페이징 정보
     * @return 해당 방법으로 채점된 결과 페이지
     */
    Page<QuestionResult> findByGradingMethodOrderByCreatedAtDesc(GradingMethod gradingMethod, Pageable pageable);
    
    /**
     * 정답 여부별 결과 조회
     * 
     * @param isCorrect 정답 여부
     * @param pageable 페이징 정보
     * @return 정답/오답 결과 페이지
     */
    Page<QuestionResult> findByIsCorrectOrderByCreatedAtDesc(Boolean isCorrect, Pageable pageable);
    
    /**
     * 특정 점수 범위의 문제별 결과 조회
     * 
     * @param minScore 최소 점수
     * @param maxScore 최대 점수
     * @param pageable 페이징 정보
     * @return 점수 범위 내 결과 페이지
     */
    @Query("SELECT qr FROM QuestionResult qr " +
           "WHERE qr.score BETWEEN :minScore AND :maxScore " +
           "ORDER BY qr.score DESC")
    Page<QuestionResult> findByScoreBetween(
        @Param("minScore") Integer minScore,
        @Param("maxScore") Integer maxScore,
        Pageable pageable
    );
    
    /**
     * AI 신뢰도 범위별 결과 조회
     * 
     * @param minConfidence 최소 신뢰도
     * @param maxConfidence 최대 신뢰도
     * @param pageable 페이징 정보
     * @return 신뢰도 범위 내 결과 페이지
     */
    @Query("SELECT qr FROM QuestionResult qr " +
           "WHERE qr.confidenceScore BETWEEN :minConfidence AND :maxConfidence " +
           "ORDER BY qr.confidenceScore DESC")
    Page<QuestionResult> findByConfidenceScoreBetween(
        @Param("minConfidence") BigDecimal minConfidence,
        @Param("maxConfidence") BigDecimal maxConfidence,
        Pageable pageable
    );
    
    /**
     * 특정 문제의 정답률 계산
     * 
     * @param questionId 문제 ID
     * @return 정답률 (0.0 ~ 1.0)
     */
    @Query("SELECT CAST(COUNT(CASE WHEN qr.isCorrect = true THEN 1 END) AS DOUBLE) / COUNT(*) " +
           "FROM QuestionResult qr " +
           "JOIN qr.examAnswer ea " +
           "WHERE ea.questionId = :questionId")
    Double calculateCorrectRateByQuestionId(@Param("questionId") UUID questionId);
    
    /**
     * 채점 방법별 통계 조회
     * 
     * @param gradingMethod 채점 방법
     * @return 해당 방법의 통계 정보
     */
    @Query("SELECT " +
           "COUNT(*) as totalCount, " +
           "COUNT(CASE WHEN qr.isCorrect = true THEN 1 END) as correctCount, " +
           "AVG(qr.score) as averageScore, " +
           "AVG(qr.confidenceScore) as averageConfidence " +
           "FROM QuestionResult qr " +
           "WHERE qr.gradingMethod = :gradingMethod")
    Object[] getStatisticsByGradingMethod(@Param("gradingMethod") GradingMethod gradingMethod);
    
    /**
     * 특정 시험 결과의 채점 완료 여부 확인
     * 
     * @param examResultId 시험 결과 ID
     * @return 모든 문제가 채점되었으면 true
     */
    @Query("SELECT CASE WHEN COUNT(qr) = COUNT(CASE WHEN qr.score IS NOT NULL THEN 1 END) THEN true ELSE false END " +
           "FROM QuestionResult qr " +
           "WHERE qr.examResult.id = :examResultId")
    Boolean isAllQuestionsGraded(@Param("examResultId") UUID examResultId);
    
    /**
     * 특정 시험 결과의 채점 진행률 계산
     * 
     * @param examResultId 시험 결과 ID
     * @return 채점 진행률 (0.0 ~ 1.0)
     */
    @Query("SELECT CAST(COUNT(CASE WHEN qr.score IS NOT NULL THEN 1 END) AS DOUBLE) / COUNT(*) " +
           "FROM QuestionResult qr " +
           "WHERE qr.examResult.id = :examResultId")
    Double calculateGradingProgress(@Param("examResultId") UUID examResultId);
    
    /**
     * 특정 답안에 대한 채점 결과 조회
     * 
     * @param examAnswerId 답안 ID
     * @return 해당 답안의 채점 결과
     */
    Optional<QuestionResult> findByExamAnswerId(UUID examAnswerId);
    
    /**
     * 수동 채점이 필요한 결과 조회
     * 
     * @param pageable 페이징 정보
     * @return 수동 채점 대상 결과 페이지
     */
    @Query("SELECT qr FROM QuestionResult qr " +
           "WHERE qr.gradingMethod = 'MANUAL' AND qr.score IS NULL " +
           "ORDER BY qr.createdAt ASC")
    Page<QuestionResult> findPendingManualGrading(Pageable pageable);
    
    /**
     * 낮은 신뢰도의 AI 채점 결과 조회 (재검토 필요)
     * 
     * @param confidenceThreshold 신뢰도 임계값
     * @param pageable 페이징 정보
     * @return 낮은 신뢰도 결과 페이지
     */
    @Query("SELECT qr FROM QuestionResult qr " +
           "WHERE qr.gradingMethod = 'AI_ASSISTED' " +
           "AND qr.confidenceScore < :confidenceThreshold " +
           "ORDER BY qr.confidenceScore ASC")
    Page<QuestionResult> findLowConfidenceAIResults(
        @Param("confidenceThreshold") BigDecimal confidenceThreshold,
        Pageable pageable
    );
    
    /**
     * 채점 방법별 개수 조회
     * 
     * @param gradingMethod 채점 방법
     * @return 해당 방법의 채점 결과 개수
     */
    long countByGradingMethod(GradingMethod gradingMethod);
    
    /**
     * 정답 개수 조회
     * 
     * @return 정답 개수
     */
    long countByIsCorrectTrue();
    
    /**
     * 오답 개수 조회
     * 
     * @return 오답 개수
     */
    long countByIsCorrectFalse();
    
    /**
     * 특정 문제의 평균 점수 계산
     * 
     * @param questionId 문제 ID
     * @return 평균 점수
     */
    @Query("SELECT AVG(qr.score) " +
           "FROM QuestionResult qr " +
           "JOIN qr.examAnswer ea " +
           "WHERE ea.questionId = :questionId AND qr.score IS NOT NULL")
    Double calculateAverageScoreByQuestionId(@Param("questionId") UUID questionId);
    
    /**
     * 시험 결과의 총점 계산
     * 
     * @param examResultId 시험 결과 ID
     * @return 총점
     */
    @Query("SELECT SUM(qr.score) " +
           "FROM QuestionResult qr " +
           "WHERE qr.examResult.id = :examResultId AND qr.score IS NOT NULL")
    Integer calculateTotalScoreByExamResultId(@Param("examResultId") UUID examResultId);
}