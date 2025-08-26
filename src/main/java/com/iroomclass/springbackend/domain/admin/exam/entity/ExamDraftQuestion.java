package com.iroomclass.springbackend.domain.admin.exam.entity;

import com.iroomclass.springbackend.domain.admin.question.entity.Question;

import jakarta.persistence.*;
import lombok.*;

/**
 * 시험지 초안 - 문제 Entity
 * 
 * 시험지 초안에 포함된 문제들을 관리합니다.
 * 각 문제의 순서와 배점을 설정할 수 있습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_draft_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamDraftQuestion {
    
    /**
     * 초안 문제 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시험지 초안과의 관계
     * ManyToOne: 여러 초안 문제가 하나의 시험지 초안에 속함
     * FetchType.LAZY: 필요할 때만 시험지 초안 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_draft_id", nullable = false)
    private ExamDraft examDraft;
    
    /**
     * 문제와의 관계
     * ManyToOne: 여러 초안 문제가 하나의 문제를 참조할 수 있음
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
}