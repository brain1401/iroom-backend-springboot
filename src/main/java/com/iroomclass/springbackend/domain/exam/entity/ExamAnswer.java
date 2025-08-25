package com.iroomclass.springbackend.domain.exam.entity;

import com.iroomclass.springbackend.domain.question.entity.Question;
import jakarta.persistence.*;
import lombok.*;

/**
 * 시험 답안 Entity
 * 
 * 학생별 문제 답안을 관리합니다.
 * AI 이미지 인식으로 답안을 추출하고, 채점 결과를 저장합니다.
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
     * ManyToOne: 여러 답안이 하나의 제출에 속함
     * FetchType.LAZY: 필요할 때만 제출 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission submission;
    
    /**
     * 문제와의 관계
     * ManyToOne: 여러 답안이 하나의 문제를 참조할 수 있음
     * FetchType.LAZY: 필요할 때만 문제 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 답안 이미지 URL
     * 촬영한 답안 이미지의 저장 경로
     * 최대 255자
     */
    @Column(length = 255)
    private String answerImageUrl;
    
    /**
     * AI 추출 답안
     * AI가 이미지에서 인식한 답안 텍스트
     * TEXT 타입으로 긴 답안 가능
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
     * 단원명
     * 표시용 단원명 (예: 정수, 유리수, 일차방정식)
     * 최대 100자
     */
    @Column(length = 100)
    private String unitName;
    
    /**
     * 난이도
     * 문제의 난이도 (하, 중, 상)
     */
    @Enumerated(EnumType.STRING)
    @Column
    private Difficulty difficulty;
    
    /**
     * 정답
     * 문제의 정답 (표시용)
     * TEXT 타입으로 긴 정답 가능
     */
    @Column(columnDefinition = "TEXT")
    private String correctAnswer;
    
    /**
     * 문제 난이도 열거형
     */
    public enum Difficulty {
        하, 중, 상
    }
}