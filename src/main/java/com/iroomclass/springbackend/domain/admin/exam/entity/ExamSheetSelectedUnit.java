package com.iroomclass.springbackend.domain.admin.exam.entity;

import com.iroomclass.springbackend.domain.admin.unit.entity.Unit;
import com.iroomclass.springbackend.common.UUIDv7Generator;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 시험지 - 선택 단원 Entity
 * 
 * 시험지에 선택된 단원들을 관리합니다.
 * 하나의 시험지에 여러 단원을 선택할 수 있으며, 중복 선택도 가능합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "exam_sheet_selected_unit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamSheetSelectedUnit {

    /**
     * 선택 단원 고유 ID
     * UUIDv7 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * 시험지와의 관계
     * ManyToOne: 여러 선택 단원이 하나의 시험지에 속함
     * FetchType.LAZY: 필요할 때만 시험지 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_sheet_id", nullable = false)
    private ExamSheet examSheet;

    /**
     * 선택된 단원과의 관계
     * ManyToOne: 여러 선택 단원이 하나의 단원을 참조할 수 있음
     * FetchType.LAZY: 필요할 때만 단원 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;
    
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