package com.iroomclass.springbackend.common;

import java.util.Objects;

/**
 * Record DTO 클래스들의 기본 인터페이스
 * 
 * <p>공통 검증 로직과 유틸리티 메서드를 제공하여 
 * Objects.requireNonNull 중복을 줄입니다.</p>
 */
public interface BaseRecord {
    
    /**
     * 필수 필드 null 검사 헬퍼 메서드
     * 
     * @param value 검증할 값
     * @param fieldName 필드명
     * @param <T> 값의 타입
     * @return 검증된 값
     * @throws IllegalArgumentException 값이 null인 경우
     */
    default <T> T requireNonNull(T value, String fieldName) {
        return Objects.requireNonNull(value, ValidationConstants.getNullCheckMessage(fieldName));
    }
    
    /**
     * 여러 필수 필드 일괄 검증 메서드
     * 
     * @param fieldChecks 필드명과 값의 배열 (fieldName1, value1, fieldName2, value2, ...)
     */
    default void requireAllNonNull(Object... fieldChecks) {
        if (fieldChecks.length % 2 != 0) {
            throw new IllegalArgumentException("필드명과 값이 쌍으로 제공되어야 합니다");
        }
        
        for (int i = 0; i < fieldChecks.length; i += 2) {
            String fieldName = (String) fieldChecks[i];
            Object value = fieldChecks[i + 1];
            requireNonNull(value, fieldName);
        }
    }
    
    /**
     * 문자열 필드 null 및 빈 문자열 검사
     * 
     * @param value 검증할 문자열
     * @param fieldName 필드명
     * @return 검증된 문자열
     * @throws IllegalArgumentException 값이 null이거나 빈 문자열인 경우
     */
    default String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다");
        }
        return value;
    }
}