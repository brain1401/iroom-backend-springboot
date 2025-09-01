package com.iroomclass.springbackend.domain.user.entity;

/**
 * 사용자 역할 열거형
 * 
 * <p>시스템 내 사용자의 역할을 정의합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
public enum UserRole {
    
    /**
     * 학생 역할
     * 시험을 치르고 결과를 조회할 수 있는 권한
     */
    STUDENT,
    
    /**
     * 관리자 역할
     * 시험 관리, 학생 관리, 통계 조회 등 모든 권한
     */
    ADMIN
}