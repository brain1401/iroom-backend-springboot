package com.iroomclass.springbackend.domain.admin.exam.entity;

import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;

import jakarta.persistence.*;
import lombok.*;

/**
 * 시험지 초안 - 선택 단원 Entity
 * 
 * 시험지 초안에 선택된 단원들을 관리합니다.
 * 하나의 시험지에 여러 단원을 선택할 수 있으며, 중복 선택도 가능합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_selected_unit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamSelectedUnit {
    
    /**
     * 선택 단원 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 시험지 초안과의 관계
     * ManyToOne: 여러 선택 단원이 하나의 시험지 초안에 속함
     * FetchType.LAZY: 필요할 때만 시험지 초안 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_draft_id", nullable = false)
    private ExamDraft examDraft;

    /**
     * 선택된 단원과의 관계
     * ManyToOne: 여러 선택 단원이 하나의 단원을 참조할 수 있음
     * FetchType.LAZY: 필요할 때만 단원 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;
}