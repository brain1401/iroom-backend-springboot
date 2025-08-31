package com.iroomclass.springbackend.domain.exam.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.iroomclass.springbackend.common.UUIDv7Generator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험결과별 문제 채점 엔티티
 * 
 * 시험 결과 내에서 각 문제별 상세 채점 정보를 관리합니다.
 * 자동 채점, 수동 채점, AI 보조 채점을 모두 지원합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_result_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QuestionResult {
    
    /**
     * 시험결과별 문제 채점 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    /**
     * 시험 결과와의 관계
     * ManyToOne: 여러 문제 결과가 하나의 시험 결과에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;
    
    /**
     * 문제와의 관계
     * ManyToOne: 여러 채점 결과가 하나의 문제에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 답안과의 관계
     * ManyToOne: 여러 채점 결과가 하나의 답안에 속함 (재채점 시)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private StudentAnswerSheet studentAnswerSheet;
    
    /**
     * 정답 여부
     * true: 정답, false: 오답, null: 미채점
     */
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    /**
     * 획득 점수
     * 해당 문제에서 획득한 점수
     */
    @Column
    private Integer score;
    
    /**
     * 문제 배점
     * 해당 문제의 만점 (question.points에서 복사)
     */
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;
    
    /**
     * 채점 방식
     * AUTO: 자동 채점 (객관식)
     * MANUAL: 수동 채점 (관리자)
     * AI_ASSISTED: AI 보조 채점 (주관식)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grading_method", nullable = false)
    @Builder.Default
    private GradingMethod gradingMethod = GradingMethod.AUTO;
    
    /**
     * 채점 코멘트
     * 문제별 피드백이나 채점 근거
     */
    @Column(name = "grading_comment", columnDefinition = "TEXT")
    private String gradingComment;
    
    /**
     * AI 채점 신뢰도
     * AI 채점 시 신뢰도 점수 (0.00 ~ 1.00)
     */
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Entity 저장 전 실행되는 메서드
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUIDv7Generator.generate();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    /**
     * Entity 업데이트 전 실행되는 메서드
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 채점 결과 업데이트
     * 
     * @param isCorrect 정답 여부
     * @param score 획득 점수
     * @param comment 채점 코멘트
     */
    public void updateGradingResult(Boolean isCorrect, Integer score, String comment) {
        this.isCorrect = isCorrect;
        this.score = score;
        this.gradingComment = comment;
    }
    
    /**
     * AI 채점 결과 업데이트
     * 
     * @param isCorrect 정답 여부
     * @param score 획득 점수
     * @param confidence AI 신뢰도
     * @param comment 채점 코멘트
     */
    public void updateAiGradingResult(Boolean isCorrect, Integer score, BigDecimal confidence, String comment) {
        this.isCorrect = isCorrect;
        this.score = score;
        this.confidenceScore = confidence;
        this.gradingComment = comment;
        this.gradingMethod = GradingMethod.AI_ASSISTED;
    }
    
    /**
     * 수동 채점 결과 업데이트
     * 
     * @param isCorrect 정답 여부
     * @param score 획득 점수
     * @param comment 채점 코멘트
     */
    public void updateManualGradingResult(Boolean isCorrect, Integer score, String comment) {
        this.isCorrect = isCorrect;
        this.score = score;
        this.gradingComment = comment;
        this.gradingMethod = GradingMethod.MANUAL;
    }
    
    /**
     * 객관식 자동 채점 수행
     * 
     * @return 채점 성공 여부
     */
    public boolean performAutoGrading() {
        if (!question.isMultipleChoice() || studentAnswerSheet.getSelectedChoice() == null) {
            return false;
        }
        
        boolean correct = question.isCorrectChoice(studentAnswerSheet.getSelectedChoice());
        this.isCorrect = correct;
        this.score = correct ? maxScore : 0;
        this.gradingMethod = GradingMethod.AUTO;
        this.confidenceScore = BigDecimal.ONE; // 객관식은 신뢰도 100%
        
        return true;
    }
    
    /**
     * 채점 완료 여부 확인
     * 
     * @return 채점이 완료되었으면 true
     */
    public boolean isGraded() {
        return isCorrect != null && score != null;
    }
    
    /**
     * 자동 채점 여부 확인
     * 
     * @return 자동 채점이면 true
     */
    public boolean isAutoGrading() {
        return gradingMethod == GradingMethod.AUTO;
    }
    
    /**
     * AI 보조 채점 여부 확인
     * 
     * @return AI 보조 채점이면 true
     */
    public boolean isAiAssistedGrading() {
        return gradingMethod == GradingMethod.AI_ASSISTED;
    }
    
    /**
     * 수동 채점 여부 확인
     * 
     * @return 수동 채점이면 true
     */
    public boolean isManualGrading() {
        return gradingMethod == GradingMethod.MANUAL;
    }
    
    /**
     * 부분 점수 비율 계산
     * 
     * @return 부분 점수 비율 (0.0 ~ 1.0)
     */
    public BigDecimal getScoreRatio() {
        if (score == null || maxScore == null || maxScore == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(score)
            .divide(BigDecimal.valueOf(maxScore), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 채점 신뢰도가 낮은지 확인
     * 
     * @return 신뢰도가 0.8 미만이면 true
     */
    public boolean hasLowConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(BigDecimal.valueOf(0.8)) < 0;
    }
    
    /**
     * 재검토가 필요한지 확인
     * AI 채점에서 신뢰도가 낮거나 부분점수인 경우
     * 
     * @return 재검토가 필요하면 true
     */
    public boolean needsReview() {
        return (isAiAssistedGrading() && hasLowConfidence()) || 
               (score != null && score > 0 && score < maxScore);
    }
    
    /**
     * 자동 채점 처리
     * 
     * @return 채점 성공 여부
     */
    public boolean processAutoGrading() {
        return performAutoGrading();
    }
    
    /**
     * 수동 채점 처리
     * 
     * @param score 획득 점수
     * @param isCorrect 정답 여부
     * @param feedback 피드백
     */
    public void processManualGrading(Integer score, Boolean isCorrect, String feedback) {
        updateManualGradingResult(isCorrect, score, feedback);
    }
    
    /**
     * AI 보조 채점 처리
     * 
     * @param score 획득 점수
     * @param isCorrect 정답 여부
     * @param confidence 신뢰도
     * @param aiAnalysis AI 분석 결과
     */
    public void processAIAssistedGrading(Integer score, Boolean isCorrect, BigDecimal confidence, String aiAnalysis) {
        updateAiGradingResult(isCorrect, score, confidence, aiAnalysis);
    }
    
    /**
     * 채점 피드백 반환
     * 
     * @return 채점 코멘트
     */
    public String getFeedback() {
        return gradingComment;
    }
    
    /**
     * AI 분석 결과 반환
     * 
     * @return AI 분석 결과 (현재는 채점 코멘트와 동일)
     */
    public String getAiAnalysis() {
        return gradingComment;
    }
    
    /**
     * 채점 방식 열거형
     */
    public enum GradingMethod {
        /**
         * 자동 채점 (주로 객관식)
         */
        AUTO,
        
        /**
         * 수동 채점 (관리자가 직접)
         */
        MANUAL,
        
        /**
         * AI 보조 채점 (AI가 채점하고 관리자가 검토)
         */
        AI_ASSISTED
    }
}