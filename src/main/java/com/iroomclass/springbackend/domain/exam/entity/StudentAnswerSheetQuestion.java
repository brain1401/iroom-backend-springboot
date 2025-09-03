package com.iroomclass.springbackend.domain.exam.entity;

import java.util.UUID;

import com.iroomclass.springbackend.common.UUIDv7Generator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
 * 학생 답안지 문제별 답안 Entity
 * 
 * 학생이 작성한 각 문제별 답안을 관리합니다.
 * 주관식과 객관식 문제의 답안을 모두 지원합니다.
 * 
 * <p>
 * 답안 유형별 특징:
 * </p>
 * <ul>
 * <li>주관식: answerText, answerImageUrl 사용</li>
 * <li>객관식: selectedChoice 사용 (선택한 번호 1~5)</li>
 * </ul>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "student_answer_sheet_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StudentAnswerSheetQuestion {

    /**
     * 답안 문제별 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * 학생 답안지와의 관계
     * ManyToOne: 여러 문제 답안이 하나의 답안지에 속함
     * FetchType.LAZY: 필요할 때만 답안지 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_answer_sheet_id", nullable = false)
    private StudentAnswerSheet studentAnswerSheet;

    /**
     * 문제와의 관계
     * ManyToOne: 여러 답안이 하나의 문제를 참조할 수 있음
     * FetchType.LAZY: 필요할 때만 문제 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

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
     * 답안 이미지 URL
     * 주관식 문제의 경우 학생이 촬영한 답안 이미지
     * 최대 500자
     */
    @Column(length = 500)
    private String answerImageUrl;

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