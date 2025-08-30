package com.iroomclass.springbackend.domain.admin.unit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.iroomclass.springbackend.common.UUIDv7Generator;

/**
 * 세부단원 Entity
 * 
 * 중분류 아래에 속하는 세부단원을 관리합니다.
 * 예시: 정수와 유리수 → 정수, 유리수, 정수와 유리수의 사칙연산
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "unit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Unit {
    
    /**
     * 세부단원 고유 ID
     * UUIDv7로 생성되는 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    /**
     * 중분류와의 관계
     * ManyToOne: 여러 세부단원이 하나의 중분류에 속함
     * FetchType.LAZY: 필요할 때만 중분류 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private UnitSubcategory subcategory;
    
    /**
     * 학년
     * 해당 단원이 속한 학년 (1, 2, 3학년)
     * 필수 입력
     */
    @Column(nullable = false)
    private Integer grade;
    
    /**
     * 세부단원명
     * 예시: "정수", "유리수", "일차방정식", "이차방정식"
     * 최대 100자, 필수 입력
     */
    @Column(nullable = false, length = 100)
    private String unitName;
    
    /**
     * 단원 코드
     * 단원을 식별하는 고유 코드
     * 예시: "MS1_NUM_INT" (중1 수와연산 정수)
     * 최대 30자, 필수 입력, 중복 불가
     */
    @Column(nullable = false, length = 30, unique = true)
    private String unitCode;
    
    /**
     * 단원 설명
     * 해당 단원에 대한 상세 설명
     * TEXT 타입으로 긴 설명 가능
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 표시 순서
     * 같은 중분류 내에서의 표시 순서
     * 예시: 1(정수), 2(유리수), 3(사칙연산)
     */
    @Column(nullable = false)
    private Integer displayOrder;
    
    /**
     * 엔티티 저장 전 UUID 자동 생성
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUIDv7Generator.generate();
        }
    }
}