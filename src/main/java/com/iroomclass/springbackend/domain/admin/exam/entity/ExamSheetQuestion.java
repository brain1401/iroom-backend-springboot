package com.iroomclass.springbackend.domain.admin.exam.entity;

import com.iroomclass.springbackend.domain.admin.question.entity.Question;

import jakarta.persistence.*;
import lombok.*;

/**
 * 시험지 - 문제 Entity
 * 
 * 시험지에 포함된 문제들을 관리합니다.
 * 각 문제의 순서와 배점을 설정할 수 있습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_sheet_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamSheetQuestion {
    
    /**
     * 시험지 문제 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시험지와의 관계
     * ManyToOne: 여러 시험지 문제가 하나의 시험지에 속함
     * FetchType.LAZY: 필요할 때만 시험지 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_sheet_id", nullable = false)
    private ExamSheet examSheet;
    
    /**
     * 문제와의 관계
     * ManyToOne: 여러 시험지 문제가 하나의 문제를 참조할 수 있음
     * FetchType.LAZY: 필요할 때만 문제 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 문제 순서
     * 시험지에서 몇 번 문제인지 (1번, 2번, 3번...)
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer seqNo;
    
    /**
     * 문제 배점
     * 이 시험지에서 해당 문제의 배점
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer points;

    /**
     * 문제 출제 순서
     * 시험지에서 실제 출제되는 순서 (seqNo와 별개의 개념)
     * 예: seqNo는 관리용 순서, questionOrder는 학생에게 보여지는 순서
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer questionOrder;

    /**
     * 문제 선택 방식
     * RANDOM: 랜덤으로 선택된 문제
     * MANUAL: 수동으로 선택된 문제
     * 기본값: MANUAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SelectionMethod selectionMethod = SelectionMethod.MANUAL;

    /**
     * 문제 선택 방식 열거형
     */
    public enum SelectionMethod {
        /**
         * 랜덤 선택
         * 시스템이 자동으로 선택한 문제
         */
        RANDOM("랜덤 선택"),
        
        /**
         * 수동 선택
         * 관리자가 직접 선택한 문제
         */
        MANUAL("수동 선택");
        
        private final String description;
        
        SelectionMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }

    /**
     * 문제 순서 업데이트
     * 
     * @param questionOrder 새로운 문제 순서 (1 이상)
     * @throws IllegalArgumentException 순서가 1보다 작을 때
     */
    public void updateQuestionOrder(Integer questionOrder) {
        if (questionOrder == null || questionOrder < 1) {
            throw new IllegalArgumentException("문제 순서는 1 이상이어야 합니다: " + questionOrder);
        }
        this.questionOrder = questionOrder;
    }

    /**
     * 문제 선택 방식 업데이트
     * 
     * @param selectionMethod 새로운 선택 방식
     */
    public void updateSelectionMethod(SelectionMethod selectionMethod) {
        this.selectionMethod = selectionMethod != null ? selectionMethod : SelectionMethod.MANUAL;
    }

    /**
     * 랜덤으로 선택된 문제인지 확인
     * 
     * @return 랜덤 선택이면 true
     */
    public boolean isRandomlySelected() {
        return SelectionMethod.RANDOM.equals(selectionMethod);
    }

    /**
     * 수동으로 선택된 문제인지 확인
     * 
     * @return 수동 선택이면 true
     */
    public boolean isManuallySelected() {
        return SelectionMethod.MANUAL.equals(selectionMethod);
    }
}