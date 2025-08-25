package com.iroomclass.springbackend.domain.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 시험 Entity
 * 
 * 시험지 초안을 실제 시험으로 발행하여 관리합니다.
 * 학생 수, QR코드, 시험 내용 등을 포함합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Exam {
    
    /**
     * 시험 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시험지 초안과의 관계
     * ManyToOne: 여러 시험이 하나의 시험지 초안에서 발행될 수 있음
     * FetchType.LAZY: 필요할 때만 시험지 초안 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_draft_id", nullable = false)
    private ExamDraft examDraft;
    
    /**
     * 시험명
     * 시험지명과 동일하게 설정
     * 최대 100자, 필수 입력
     */
    @Column(nullable = false, length = 100)
    private String examName;
    
    /**
     * 학년
     * 해당 시험이 몇 학년용인지 (1, 2, 3학년)
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer grade;
    
    /**
     * 시험 내용
     * 시험 관련 메모나 설명
     * TEXT 타입으로 긴 내용 가능
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 학생 수
     * 시험 등록 시 입력받는 전체 학생 수
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer studentCount;
    
    /**
     * QR코드 URL
     * 학생이 접속하는 QR코드 링크
     * 최대 255자
     */
    @Column(length = 255)
    private String qrCodeUrl;
    
    /**
     * 등록일시
     * 시험이 등록된 날짜와 시간
     * 자동으로 현재 시간이 설정됩니다.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Entity 저장 전 실행되는 메서드
     * 등록일시를 자동으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}