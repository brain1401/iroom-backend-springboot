package com.iroomclass.springbackend.domain.admin.unit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 단원 중분류 Entity
 * 
 * 대분류 아래에 속하는 중분류를 관리합니다.
 * 예시: 수와 연산 → 정수와 유리수, 문자와 식, 방정식
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "unit_subcategory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UnitSubcategory {
    
    /**
     * 중분류 고유 ID
     * 자동 증가하는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 대분류와의 관계
     * ManyToOne: 여러 중분류가 하나의 대분류에 속함
     * FetchType.LAZY: 필요할 때만 대분류 정보를 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private UnitCategory category;
    
    /**
     * 중분류명
     * 예시: "정수와 유리수", "문자와 식", "방정식", "부등식"
     * 최대 100자, 필수 입력
     */
    @Column(nullable = false, length = 100)
    private String subcategoryName;
    
    /**
     * 표시 순서
     * 같은 대분류 내에서의 표시 순서
     * 예시: 1(정수와 유리수), 2(문자와 식), 3(방정식)
     */
    @Column(nullable = false)
    private Integer displayOrder;
    
    /**
     * 중분류 설명
     * 해당 중분류에 대한 상세 설명
     * 최대 200자
     */
    @Column(length = 200)
    private String description;
}