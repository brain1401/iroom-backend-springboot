package com.iroomclass.springbackend.domain.exam.repository.projection;

import java.util.UUID;

/**
 * 단원 정보 조회를 위한 Projection 인터페이스
 * 
 * <p>성능 최적화를 위해 필요한 단원 필드만 선택하여 조회합니다.
 * Spring Data JPA의 Interface-based Projection을 활용하여 
 * N+1 문제 없이 효율적으로 데이터를 가져옵니다.</p>
 */
public interface UnitProjection {
    
    /**
     * 단원 고유 식별자
     */
    UUID getId();
    
    /**
     * 단원명
     */
    String getUnitName();
    
    /**
     * 단원 코드
     */
    String getUnitCode();
    
    /**
     * 학년
     */
    Integer getGrade();
    
    /**
     * 단원 설명
     */
    String getDescription();
    
    /**
     * 표시 순서
     */
    Integer getDisplayOrder();
    
    /**
     * 중분류 정보 Projection
     */
    UnitSubcategoryProjection getSubcategory();
    
    /**
     * 중분류 정보 Projection 인터페이스
     */
    interface UnitSubcategoryProjection {
        UUID getId();
        String getSubcategoryName();
        String getDescription();
        Integer getDisplayOrder();
        
        /**
         * 대분류 정보 Projection
         */
        UnitCategoryProjection getCategory();
    }
    
    /**
     * 대분류 정보 Projection 인터페이스
     */
    interface UnitCategoryProjection {
        UUID getId();
        String getCategoryName();
        String getDescription();
        Integer getDisplayOrder();
    }
}