package com.iroomclass.springbackend.domain.exam.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.iroomclass.springbackend.common.UUIDv7Generator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 결과 엔티티
 * 
 * 제출된 시험에 대한 전체 AI 채점 결과를 관리합니다.
 * 재채점 히스토리를 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamResult {
    
    /**
     * 시험 결과 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    /**
     * 시험 제출과의 관계
     * ManyToOne: 여러 채점 결과가 하나의 제출에 속함 (재채점 시나리오)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission examSubmission;
    
    /**
     * 시험지와의 관계
     * ManyToOne: 여러 채점 결과가 하나의 시험지에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_sheet_id", nullable = false)
    private ExamSheet examSheet;
    
    
    /**
     * 채점일시
     * 채점이 완료된 날짜와 시간
     */
    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;
    
    /**
     * 총점
     * 모든 문제 점수의 합계
     */
    @Column(name = "total_score")
    private Integer totalScore;
    
    /**
     * 채점 상태
     * PENDING: 채점 대기
     * IN_PROGRESS: 채점 진행중
     * COMPLETED: 채점 완료
     * REGRADED: 재채점
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ResultStatus status = ResultStatus.PENDING;
    
    /**
     * 채점 코멘트
     * 전체 시험에 대한 코멘트나 피드백
     */
    @Column(name = "grading_comment", columnDefinition = "TEXT")
    private String gradingComment;
    
    /**
     * 재채점 버전
     * 동일한 제출물에 대한 채점 버전 관리
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;
    
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
     * 문제별 채점 결과 목록
     * OneToMany: 하나의 시험 결과에 여러 문제별 결과가 속함
     */
    @OneToMany(mappedBy = "examResult", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionResult> questionResults = new ArrayList<>();
    
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
        if (gradedAt == null) {
            gradedAt = now;
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
     * 채점 상태 업데이트
     * 
     * @param status 새로운 채점 상태
     */
    public void updateStatus(ResultStatus status) {
        this.status = status;
        if (status == ResultStatus.COMPLETED) {
            this.gradedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 총점 업데이트
     * 문제별 점수 합계로 자동 계산
     */
    public void calculateAndUpdateTotalScore() {
        this.totalScore = questionResults.stream()
            .mapToInt(qr -> qr.getScore() != null ? qr.getScore() : 0)
            .sum();
    }
    
    /**
     * 채점 코멘트 업데이트
     * 
     * @param comment 채점 코멘트
     */
    public void updateGradingComment(String comment) {
        this.gradingComment = comment;
    }
    
    /**
     * 재채점을 위한 새 버전 생성
     * 
     * @return 새 버전의 ExamResult
     */
    public ExamResult createNewVersionForRegrading() {
        return ExamResult.builder()
            .examSubmission(this.examSubmission)
            .examSheet(this.examSheet)
            .status(ResultStatus.PENDING)
            .version(this.version + 1)
            .build();
    }
    
    /**
     * AI 자동 채점 여부 확인
     * 모든 채점이 AI에 의해 자동으로 수행됨
     * 
     * @return 항상 true (모든 채점이 AI 자동 채점)
     */
    public boolean isAutoGrading() {
        return true;
    }
    
    /**
     * 채점 완료 여부 확인
     * 
     * @return 채점이 완료되었으면 true
     */
    public boolean isCompleted() {
        return status == ResultStatus.COMPLETED;
    }
    
    /**
     * 재채점 여부 확인
     * 
     * @return 재채점이면 true
     */
    public boolean isRegraded() {
        return status == ResultStatus.REGRADED || version > 1;
    }
    
    /**
     * 채점 진행률 계산
     * 
     * @return 채점 진행률 (0.0 ~ 1.0)
     */
    public BigDecimal getGradingProgress() {
        if (questionResults.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        long gradedCount = questionResults.stream()
            .filter(qr -> qr.getScore() != null)
            .count();
        
        return BigDecimal.valueOf(gradedCount)
            .divide(BigDecimal.valueOf(questionResults.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 문제별 채점 결과 추가
     * 
     * @param questionResult 문제별 채점 결과
     */
    public void addQuestionResult(QuestionResult questionResult) {
        questionResults.add(questionResult);
    }
    
    /**
     * 채점 상태 열거형
     */
    public enum ResultStatus {
        /**
         * 채점 대기
         */
        PENDING,
        
        /**
         * 채점 진행중
         */
        IN_PROGRESS,
        
        /**
         * 채점 완료
         */
        COMPLETED,
        
        /**
         * 재채점
         */
        REGRADED
    }
}