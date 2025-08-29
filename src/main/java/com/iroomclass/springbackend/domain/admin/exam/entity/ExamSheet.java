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
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    /**
     * Entity 업데이트 전 실행되는 메서드
     * 수정일시를 자동으로 설정합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}