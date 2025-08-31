package com.iroomclass.springbackend.domain.curriculum.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.iroomclass.springbackend.common.UUIDv7Generator;

/**
 * 단원 대분류 Entity
 * 
 * 중학교 수학 교육과정의 대분류를 관리합니다.
 * 예시: 수와 연산, 문자와 식, 함수, 기하, 통계와 확률
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Entity
@Table(name = "unit_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UnitCategory {
    
    /**
     * 대분류 고유 ID
     * UUIDv7로 생성되는 기본키
     */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    /**
     * 대분류명
     * 예시: "수와 연산", "문자와 식", "함수", "기하", "통계와 확률"
     * 최대 50자, 필수 입력
     */
    @Column(nullable = false, length = 50)
    private String categoryName;
    
    /**
     * 표시 순서
     * 화면에 표시될 순서를 결정합니다.
     * 예시: 1(수와 연산), 2(문자와 식), 3(함수), 4(기하), 5(통계와 확률)
     */
    @Column(nullable = false)
    private Integer displayOrder;
    
    /**
     * 대분류 설명
     * 해당 대분류에 대한 상세 설명
     * 최대 200자
     */
    @Column(length = 200)
    private String description;
    
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