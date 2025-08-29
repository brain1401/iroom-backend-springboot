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
 * 주관식과 객관식 문제의 답안을 모두 지원하며, AI 이미지 인식 결과와 채점 결과를 포함합니다.
 * 
 * <p>답안 유형별 특징:</p>
 * <ul>
 *   <li>주관식: answerText 사용 (AI가 이미지에서 인식한 텍스트)</li>
 *   <li>객관식: selectedChoice 사용 (선택한 번호 1~5)</li>
 * </ul>
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
     * AI가 추출한 답안 텍스트 (주관식용)
     * AI가 이미지에서 인식한 텍스트 답안
     * 주관식 문제일 때만 사용
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
     * 선택한 답안 번호 (객관식용)
     * 객관식 문제에서 학생이 선택한 번호 (1, 2, 3, 4, 5)
     * 객관식 문제일 때만 사용
     */
    @Column
    private Integer selectedChoice;
    
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

    /**
     * 객관식 답안 선택 업데이트
     * 
     * @param selectedChoice 선택한 답안 번호 (1~5)
     */
    public void updateSelectedChoice(Integer selectedChoice) {
        if (selectedChoice != null && (selectedChoice < 1 || selectedChoice > 5)) {
            throw new IllegalArgumentException("선택한 답안 번호는 1~5 사이여야 합니다: " + selectedChoice);
        }
        this.selectedChoice = selectedChoice;
    }

    /**
     * 객관식 문제 여부 확인
     * 
     * @return 객관식 문제이면 true, 아니면 false
     */
    public boolean isMultipleChoiceQuestion() {
        return question != null && question.isMultipleChoice();
    }

    /**
     * 주관식 문제 여부 확인
     * 
     * @return 주관식 문제이면 true, 아니면 false
     */
    public boolean isSubjectiveQuestion() {
        return question != null && question.isSubjective();
    }

    /**
     * 답안이 제출되었는지 확인
     * 
     * @return 답안이 제출되었으면 true, 아니면 false
     */
    public boolean hasAnswer() {
        if (isMultipleChoiceQuestion()) {
            return selectedChoice != null;
        } else {
            return answerText != null && !answerText.trim().isEmpty();
        }
    }

    /**
     * 객관식 자동 채점
     * 객관식 문제인 경우 자동으로 채점하고 결과를 업데이트
     * 
     * @return 채점이 실행되었으면 true, 아니면 false
     */
    public boolean autoGradeMultipleChoice() {
        if (!isMultipleChoiceQuestion() || selectedChoice == null) {
            return false;
        }

        boolean correct = question.isCorrectChoice(selectedChoice);
        this.isCorrect = correct;
        this.score = correct ? 100 : 0; // 기본 점수: 맞으면 100, 틀리면 0
        
        return true;
    }

    /**
     * 답안 내용 반환 (타입별)
     * 
     * @return 객관식이면 선택 번호, 주관식이면 텍스트 답안
     */
    public String getAnswerContent() {
        if (isMultipleChoiceQuestion()) {
            return selectedChoice != null ? selectedChoice.toString() : null;
        } else {
            return answerText;
        }
    }

    /**
     * 답안 상태 확인
     * 
     * @return 답안 상태 (ANSWERED, UNANSWERED, GRADED)
     */
    public AnswerStatus getAnswerStatus() {
        if (!hasAnswer()) {
            return AnswerStatus.UNANSWERED;
        }
        
        if (isCorrect != null) {
            return AnswerStatus.GRADED;
        }
        
        return AnswerStatus.ANSWERED;
    }

    /**
     * 답안 상태 열거형
     */
    public enum AnswerStatus {
        /**
         * 답안 미제출
         */
        UNANSWERED,
        
        /**
         * 답안 제출됨 (미채점)
         */
        ANSWERED,
        
        /**
         * 채점 완료
         */
        GRADED
    }
}
