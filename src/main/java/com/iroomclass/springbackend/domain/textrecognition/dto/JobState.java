package com.iroomclass.springbackend.domain.textrecognition.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 텍스트 인식 작업 상태 정보
 * 메모리에서 작업 상태를 관리하기 위한 내부 DTO
 */
public record JobState(
    String jobId,
    String aiJobId,  // AI 서버에서 생성한 작업 ID
    JobStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime completedAt,
    String originalFilename,
    Long fileSize,
    String callbackUrl,
    List<AnswerDto> answers,
    MetadataDto metadata,
    String errorMessage
) {
    
    /**
     * 새로운 작업 상태 생성
     */
    public static JobState create(String jobId, String originalFilename, Long fileSize, String callbackUrl) {
        LocalDateTime now = LocalDateTime.now();
        return new JobState(
            jobId,
            null,  // AI jobId는 나중에 설정
            JobStatus.SUBMITTED,
            now,
            now,
            null,
            originalFilename,
            fileSize,
            callbackUrl,
            null,
            null,
            null
        );
    }
    
    /**
     * AI 작업 ID 설정
     */
    public JobState withAiJobId(String aiJobId) {
        return new JobState(
            jobId,
            aiJobId,
            status,
            createdAt,
            LocalDateTime.now(),
            completedAt,
            originalFilename,
            fileSize,
            callbackUrl,
            answers,
            metadata,
            errorMessage
        );
    }
    
    /**
     * 상태 업데이트
     */
    public JobState updateStatus(JobStatus newStatus) {
        return new JobState(
            jobId,
            aiJobId,
            newStatus,
            createdAt,
            LocalDateTime.now(),
            completedAt,
            originalFilename,
            fileSize,
            callbackUrl,
            answers,
            metadata,
            errorMessage
        );
    }
    
    /**
     * 완료 처리 (결과 포함)
     */
    public JobState complete(List<AnswerDto> answers, MetadataDto metadata) {
        return new JobState(
            jobId,
            aiJobId,
            JobStatus.COMPLETED,
            createdAt,
            LocalDateTime.now(),
            LocalDateTime.now(),
            originalFilename,
            fileSize,
            callbackUrl,
            answers,
            metadata,
            null
        );
    }
    
    /**
     * 완료 처리 (결과 없이)
     */
    public JobState complete() {
        return new JobState(
            jobId,
            aiJobId,
            JobStatus.COMPLETED,
            createdAt,
            LocalDateTime.now(),
            LocalDateTime.now(),
            originalFilename,
            fileSize,
            callbackUrl,
            answers,
            metadata,
            null
        );
    }
    
    /**
     * 실패 처리
     */
    public JobState fail(String errorMessage) {
        return new JobState(
            jobId,
            aiJobId,
            JobStatus.FAILED,
            createdAt,
            LocalDateTime.now(),
            LocalDateTime.now(),
            originalFilename,
            fileSize,
            callbackUrl,
            null,
            null,
            errorMessage
        );
    }
    
    /**
     * 작업이 진행 중인지 확인
     */
    public boolean isInProgress() {
        return status.isInProgress();
    }
    
    /**
     * 작업이 완료되었는지 확인
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }
    
    // Getter 메서드들 (Service에서 사용)
    public JobStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public String getAiJobId() {
        return aiJobId;
    }
}