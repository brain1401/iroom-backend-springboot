package com.iroomclass.springbackend.domain.exam.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 시험지 문서 Entity
 * 
 * 시험지 생성 시 만들어지는 3가지 문서를 관리합니다.
 * 1. 학생 답안지 (QR코드 포함)
 * 2. 시험 문제지
 * 3. 시험 답안 (정답)
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_document")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamDocument {
    
    /**
     * 시험지 문서 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시험지 초안과의 관계
     * ManyToOne: 여러 문서가 하나의 시험지 초안에 속함
     * FetchType.LAZY: 필요할 때만 시험지 초안 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_draft_id", nullable = false)
    private ExamDraft examDraft;
    
    /**
     * 문서 종류
     * ANSWER_SHEET: 답안지, QUESTION_PAPER: 문제지, ANSWER_KEY: 답안
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
    
    /**
     * 문서 내용
     * HTML 형태로 저장된 문서 내용
     * LONGTEXT 타입으로 긴 내용 저장 가능
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String documentContent;
    
    /**
     * QR코드 URL
     * 답안지에만 해당하며, 제출 화면으로 이동하는 링크
     * 최대 255자
     */
    @Column(length = 255)
    private String qrCodeUrl;
    
    /**
     * 문서 종류 열거형
     */
    public enum DocumentType {
        ANSWER_SHEET,    // 답안지
        QUESTION_PAPER,  // 문제지
        ANSWER_KEY       // 답안
    }
}