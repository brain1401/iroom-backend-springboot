package com.iroomclass.springbackend.domain.exam.repository.projection;

import java.util.UUID;

/**
 * 단원 이름 정보 조회를 위한 Projection 인터페이스 (간소화된 버전)
 * 
 * <p>성능 최적화를 위해 단원의 기본 정보만 선택하여 조회합니다.
 * 복잡한 계층 구조 정보는 제외하고 필수 정보만 포함합니다.</p>
 */
public interface UnitNameProjection {
    
    /**
     * 단원 고유 식별자
     */
    UUID getId();
    
    /**
     * 단원명
     */
    String getUnitName();
}