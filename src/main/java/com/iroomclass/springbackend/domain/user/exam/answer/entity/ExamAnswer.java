package com.iroomclass.springbackend.domain.user.exam.answer.entity;

import java.time.LocalDateTime;

import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 답안 Entity
 * 
 * 학생이 제출한 시험 답안을 관리합니다.
 * AI 이미지 인식 결과와 채점 결과를 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamAnswer {
    
    /**
     * 답안 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시험 제출과의 관계
     * ManyToOne: 여러 답안이 하나의 시험 제출에 속함
     * FetchType.LAZY: 필요할 때만 시험 제출 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission examSubmission;
    
    /**
     * 문제와의 관계
     * ManyToOne: 여러 답안이 하나의 문제에 속함
     * FetchType.LAZY: 필요할 때만 문제 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 답안 이미지 URL
     * 촬영한 답안 이미지의 URL
     * 최대 255자
     */
    @Column(length = 255)
    private String answerImageUrl;
    
    /**
     * AI가 추출한 답안 텍스트
     * AI가 이미지에서 인식한 텍스트 답안
     * TEXT 타입으로 긴 내용 가능
     */
    @Column(columnDefinition = "TEXT")
    private String answerText;
    
    /**
     * 정답 여부
     * 0: 오답, 1: 정답
     */
    @Column
    private Boolean isCorrect;
    
    /**
     * 획득 점수
     * 해당 문제에서 획득한 점수
     */
    @Column
    private Integer score;
    
    /**
     * Entity 저장 전 실행되는 메서드
     * 기본값 설정
     */
    @PrePersist
    protected void onCreate() {
        if (isCorrect == null) {
            isCorrect = false;
        }
        if (score == null) {
            score = 0;
        }
    }
    
    /**
     * AI 인식 결과 업데이트
     */
    public void updateAnswerText(String answerText) {
        this.answerText = answerText;
    }
    
    /**
     * 이미지 URL 업데이트
     */
    public void updateImageUrl(String answerImageUrl) {
        this.answerImageUrl = answerImageUrl;
    }
    
    /**
     * 채점 결과 업데이트
     */
    public void updateGrading(Boolean isCorrect, Integer score) {
        this.isCorrect = isCorrect;
        this.score = score;
    }
}
