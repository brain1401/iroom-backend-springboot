package com.iroomclass.springbackend.domain.exam.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.iroomclass.springbackend.domain.auth.entity.Student;
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
 * 시험 제출 Entity
 * 
 * 학생별 시험 제출 기록을 관리합니다. (순수 제출 정보만)
 * 시험당 1회 제출 제한이며, User 엔티티와의 관계로 학생을 식별합니다.
 * 채점 결과는 별도의 ExamGrading 엔티티에서 관리합니다.
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
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
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
     * ManyToOne: 여러 시험 제출이 하나의 학생에 속함
     * FetchType.LAZY: 필요할 때만 학생 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    /**
     * 제출일시
     * 시험 답안이 제출된 날짜와 시간
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt;
    
    /**
     * 총 점수
     * 채점 완료 후 계산된 총점
     */
    @Column(name = "total_score")
    private Integer totalScore;
    
    /**
     * Entity 저장 전 실행되는 메서드
     * UUID 및 제출일시를 자동으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUIDv7Generator.generate();
        }
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 최종 제출일시 업데이트
     * 답안 작성 완료 후 최종 제출 시 호출됩니다.
     */
    public void updateSubmittedAt() {
        this.submittedAt = LocalDateTime.now();
    }
    
    /**
     * 총 점수 반환
     * 
     * @return 총 점수
     */
    public Integer getTotalScore() {
        return totalScore;
    }
    
    /**
     * 총 점수 업데이트
     * 
     * @param totalScore 총 점수
     */
    public void updateTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
    
    /**
     * 학생 정보 반환
     * 
     * @return 학생 정보
     */
    public Student getStudent() {
        return student;
    }
}
