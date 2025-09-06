package com.iroomclass.springbackend.domain.textrecognition.dto;

/**
 * 텍스트 인식 작업 상태
 */
public enum JobStatus {
    
    /**
     * 제출됨 - 작업이 생성되었으나 아직 처리 시작 전
     */
    SUBMITTED,
    
    /**
     * 처리 중 - AI 서버에서 글자인식 처리 중
     */
    PROCESSING,
    
    /**
     * 완료 - 글자인식 처리가 성공적으로 완료됨
     */
    COMPLETED,
    
    /**
     * 실패 - 글자인식 처리 중 오류 발생
     */
    FAILED;
    
    /**
     * 문자열로부터 JobStatus 변환
     */
    public static JobStatus fromString(String status) {
        try {
            return JobStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 작업 상태: " + status);
        }
    }
    
    /**
     * 완료 상태인지 확인
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED;
    }
    
    /**
     * 진행 중 상태인지 확인  
     */
    public boolean isInProgress() {
        return this == SUBMITTED || this == PROCESSING;
    }
}