package com.iroomclass.springbackend.domain.exam.repository.projection;

import java.util.UUID;

/**
 * 단원 기본 정보 조회를 위한 경량 Projection 인터페이스
 * 
 * <p>계층 구조 정보 없이 단원 자체 정보만 필요한 경우 사용합니다.
 * 더 빠른 조회 성능과 적은 메모리 사용량을 제공합니다.</p>
 */
public interface UnitBasicProjection {
    
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
     * 표시 순서
     */
    Integer getDisplayOrder();
}