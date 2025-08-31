package com.iroomclass.springbackend.domain.exam.entity;

import jakarta.persistence.*;
import com.iroomclass.springbackend.common.UUIDv7Generator;
import lombok.*;

import java.util.UUID;

/**
 * 시험지 문서 Entity
 * 
 * 시험지 생성 시 만들어지는 3가지 문서를 관리합니다.
 * 1. 학생이 제출하는 답안지 (주관식만, QR코드 포함)
 * 2. 실제 학생이 풀었던 시험 문제지
 * 3. DB에 들어있는 시험의 정답지
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
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    /**
     * 시험지와의 관계
     * ManyToOne: 여러 문서가 하나의 시험지에 속함
     * FetchType.LAZY: 필요할 때만 시험지 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_sheet_id", nullable = false)
    private ExamSheet examSheet;
    
    /**
     * 문서 종류
     * STUDENT_ANSWER_SHEET: 학생 답안지, EXAM_SHEET: 시험 문제지, CORRECT_ANSWER_SHEET: 정답지
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
     * LONGTEXT 타입으로 긴 QR 코드 URL 저장 가능
     */
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeUrl;
    
    /**
     * 문서 종류 열거형
     */
    public enum DocumentType {
        /**
         * 학생이 시험에 제출하는 답안지 (주관식만)
         * QR코드가 포함되어 있어 제출 화면으로 이동 가능
         */
        STUDENT_ANSWER_SHEET,
        
        /**
         * 실제 학생이 풀었던 시험 문제지
         * 객관식과 주관식 문제가 모두 포함
         */
        EXAM_SHEET,
        
        /**
         * 데이터베이스에 저장된 시험의 정답지
         * 채점 및 검증용도로 사용
         */
        CORRECT_ANSWER_SHEET
    }
    
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
}