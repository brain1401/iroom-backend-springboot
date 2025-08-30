package com.iroomclass.springbackend.domain.user.exam.entity;

import java.time.LocalDateTime;

import com.iroomclass.springbackend.domain.admin.exam.entity.Exam;
import com.iroomclass.springbackend.domain.user.info.entity.User;

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
 * 시험 제출 Entity
 * 
 * 학생별 시험 제출 기록을 관리합니다.
 * 시험당 1회 제출 제한이며, User 엔티티와의 관계로 학생을 식별합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_submission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamSubmission {
    
    /**
     * 제출 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시험과의 관계
     * ManyToOne: 여러 제출이 하나의 시험에 속함
     * FetchType.LAZY: 필요할 때만 시험 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    /**
     * 학생 정보
     * ManyToOne: 여러 시험 제출이 하나의 사용자에 속함
     * FetchType.LAZY: 필요할 때만 사용자 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 제출일시
     * 시험 답안이 제출된 날짜와 시간
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt;
    
    /**
     * 총점
     * 자동 채점 후 계산된 총점
     */
    @Column
    private Integer totalScore;
    
    /**
     * Entity 저장 전 실행되는 메서드
     * 제출일시를 자동으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
    
    /**
     * 최종 제출일시 업데이트
     * 답안 작성 완료 후 최종 제출 시 호출됩니다.
     */
    public void updateSubmittedAt() {
        this.submittedAt = LocalDateTime.now();
    }
}
