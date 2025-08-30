package com.iroomclass.springbackend.domain.admin.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 시험지 Entity
 * 
 * 시험지 생성을 위한 정보를 관리합니다.
 * 총점은 100점으로 고정되며, 최대 30문제까지 설정 가능합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_sheet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamSheet {

    /**
     * 시험지 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 시험지 이름
     * 예시: "1학년 중간고사", "2학년 기말고사"
     * 최대 100자, 필수 입력
     */
    @Column(nullable = false, length = 100)
    private String examName;

    /**
     * 학년
     * 해당 시험지가 몇 학년용인지 (1, 2, 3학년)
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer grade;

    /**
     * 총 문제 개수
     * 최대 30문제까지 설정 가능
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer totalQuestions;
    
    /**
     * 객관식 문제 개수
     * 객관식 문제의 개수 (5지 선다형)
     * 기본값: 0
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer multipleChoiceCount = 0;
    
    /**
     * 주관식 문제 개수  
     * 주관식 문제의 개수 (서술형)
     * 기본값: 0
     */
    @Column(nullable = false)  
    @Builder.Default
    private Integer subjectiveCount = 0;


    
    /**
     * 생성일시
     * 시험지가 생성된 날짜와 시간
     * 자동으로 현재 시간이 설정됩니다.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     * 시험지가 마지막으로 수정된 날짜와 시간
     * 자동으로 현재 시간이 설정됩니다.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Entity 저장 전 실행되는 메서드
     * 생성일시와 수정일시를 자동으로 설정합니다.
     * 이미 설정된 값이 있으면 유지합니다.
     */
    @PrePersist
    protected void onCreate() {
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
     * 수정일시를 자동으로 설정합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 문제 타입별 개수 업데이트
     * 총 문제 개수와 타입별 개수의 일관성을 보장합니다.
     * 
     * @param totalQuestions 총 문제 개수
     * @param multipleChoiceCount 객관식 문제 개수
     * @param subjectiveCount 주관식 문제 개수
     * @throws IllegalArgumentException 타입별 개수의 합이 총 개수와 다를 때
     */
    public void updateQuestionCounts(Integer totalQuestions, Integer multipleChoiceCount, Integer subjectiveCount) {
        if (multipleChoiceCount < 0 || subjectiveCount < 0) {
            throw new IllegalArgumentException("문제 개수는 0 이상이어야 합니다");
        }
        
        if (multipleChoiceCount + subjectiveCount != totalQuestions) {
            throw new IllegalArgumentException(
                String.format("문제 타입별 개수의 합이 총 문제 개수와 일치하지 않습니다: 객관식(%d) + 주관식(%d) != 총문제수(%d)", 
                    multipleChoiceCount, subjectiveCount, totalQuestions)
            );
        }
        
        this.totalQuestions = totalQuestions;
        this.multipleChoiceCount = multipleChoiceCount;
        this.subjectiveCount = subjectiveCount;
    }

    /**
     * 문제 개수 유효성 검증
     * 
     * @return 유효하면 true, 아니면 false
     */
    public boolean isQuestionCountsValid() {
        return multipleChoiceCount != null && subjectiveCount != null 
            && multipleChoiceCount >= 0 && subjectiveCount >= 0
            && multipleChoiceCount + subjectiveCount == totalQuestions;
    }

    /**
     * 객관식 문제가 있는지 확인
     * 
     * @return 객관식 문제가 있으면 true
     */
    public boolean hasMultipleChoiceQuestions() {
        return multipleChoiceCount != null && multipleChoiceCount > 0;
    }

    /**
     * 주관식 문제가 있는지 확인
     * 
     * @return 주관식 문제가 있으면 true
     */
    public boolean hasSubjectiveQuestions() {
        return subjectiveCount != null && subjectiveCount > 0;
    }
}