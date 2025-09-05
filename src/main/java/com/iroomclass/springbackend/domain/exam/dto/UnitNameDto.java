package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.UUID;

/**
 * 단원 이름 정보 DTO (간소화된 버전)
 * 
 * <p>시험에 포함된 단원의 기본 정보만 제공합니다.
 * 복잡한 계층 구조 정보는 제외하고 필수 정보만 포함합니다.</p>
 */
@Schema(description = "단원 이름 정보 DTO")
public record UnitNameDto(
    @Schema(description = "단원 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "단원명", example = "일차방정식")
    String unitName
) implements Serializable {

    /**
     * Compact Constructor - 필수 필드 검증
     */
    public UnitNameDto {
        if (id == null) throw new IllegalArgumentException("id는 필수입니다");
        if (unitName == null || unitName.isBlank()) throw new IllegalArgumentException("unitName은 필수입니다");
    }
}