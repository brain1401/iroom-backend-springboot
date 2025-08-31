package com.iroomclass.springbackend.domain.user.exam.answer.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.iroomclass.springbackend.domain.user.exam.entity.ExamSubmission;
import com.iroomclass.springbackend.domain.admin.question.entity.Question;
import com.iroomclass.springbackend.common.UUIDv7Generator;

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
 * 학생 답안지 Entity
 * 
 * 학생이 제출한 시험 답안을 관리합니다. (순수 답안 정보만)
 * 주관식과 객관식 문제의 답안을 모두 지원하며, AI 이미지 인식 결과를 포함합니다.
 * 채점 결과는 별도의 QuestionGrading 엔티티에서 관리합니다.
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
@Table(name = "student_answer_sheet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StudentAnswerSheet {
    
    /**
     * 답안 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
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
     * ManyToOne: 여러 답안이 하나의 문제를 참조할 수 있음
     * FetchType.LAZY: 필요할 때만 문제 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 답안 이미지 URL
     * 주관식 문제의 경우 학생이 촬영한 답안 이미지
     * 최대 500자, 주관식 문제에서 필수
     */
    @Column(length = 500)
    private String answerImageUrl;
    
    /**
     * AI가 인식한 답안 텍스트
     * 주관식 문제의 경우 AI가 이미지에서 인식한 텍스트
     * 최대 1000자
     */
    @Column(length = 1000)
    private String answerText;
    
    /**
     * 객관식 선택 답안
     * 객관식 문제의 경우 선택한 번호 (1~5)
     */
    @Column
    private Integer selectedChoice;
    
    /**
     * AI 해답 처리 과정
     * AI가 문제를 분석하고 해결하는 과정을 설명
     */
    @Column(columnDefinition = "TEXT")
    private String aiSolutionProcess;
    
    /**
     * 정답 여부
     * AI 채점 결과
     */
    @Column
    private Boolean isCorrect;
    
    /**
     * 획득 점수
     * AI 채점을 통해 부여된 점수
     */
    @Column
    private Integer score;
    
    /**
     * Entity 저장 전 실행되는 메서드
     * UUID를 자동으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUIDv7Generator.generate();
        }
    }
    
    /**
     * 답안 텍스트 업데이트 (주관식용)
     * 
     * @param answerText 새로운 답안 텍스트
     */
    public void updateAnswerText(String answerText) {
        this.answerText = answerText;
    }
    
    /**
     * 선택 답안 업데이트 (객관식용)
     * 
     * @param selectedChoice 새로운 선택 답안
     */
    public void updateSelectedChoice(Integer selectedChoice) {
        this.selectedChoice = selectedChoice;
    }
    
    /**
     * 이미지 URL 업데이트
     * 
     * @param imageUrl 새로운 이미지 URL
     */
    public void updateImageUrl(String imageUrl) {
        this.answerImageUrl = imageUrl;
    }
    
    /**
     * 채점 결과 업데이트
     * 
     * @param isCorrect 정답 여부
     * @param score 획득 점수
     */
    public void updateGradingResult(Boolean isCorrect, Integer score) {
        this.isCorrect = isCorrect;
        this.score = score;
    }
    
    /**
     * 답안 내용 반환 (문제 유형에 따라)
     * 
     * @return 답안 내용
     */
    public String getAnswerContent() {
        if (selectedChoice != null) {
            return selectedChoice.toString();
        } else if (answerText != null) {
            return answerText;
        }
        return null;
    }
    
    /**
     * 객관식 문제 여부 확인
     * 
     * @return 객관식 문제면 true
     */
    public boolean isMultipleChoiceQuestion() {
        return question != null && question.isMultipleChoice();
    }
    
    /**
     * 답안 제출 여부 확인
     * 
     * @return 답안이 제출되었으면 true
     */
    public boolean hasAnswer() {
        return selectedChoice != null || answerText != null;
    }
    
    /**
     * 문제 ID 반환
     * 
     * @return 문제 ID
     */
    public UUID getQuestionId() {
        return question != null ? question.getId() : null;
    }
    

    
    /**
     * 제출된 답안 반환
     * 
     * @return 제출된 답안
     */
    public String getSubmittedAnswer() {
        return getAnswerContent();
    }
    
    /**
     * 답안 유형 반환
     * 
     * @return 답안 유형
     */
    public AnswerType getAnswerType() {
        if (isMultipleChoiceQuestion()) {
            return AnswerType.MULTIPLE_CHOICE;
        } else {
            return AnswerType.SUBJECTIVE;
        }
    }
    
    /**
     * 최대 점수 반환 (문제에서 가져옴)
     * 
     * @return 최대 점수
     */
    public Integer getMaxScore() {
        return question != null ? question.getPoints() : 0;
    }

    /**
     * 답안 유형 열거형
     */
    public enum AnswerType {
        /**
         * 객관식 답안
         */
        MULTIPLE_CHOICE,
        
        /**
         * 주관식 답안
         */
        SUBJECTIVE
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
         * 답안 제출됨
         */
        ANSWERED
    }
}