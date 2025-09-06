package com.iroomclass.springbackend.domain.textrecognition.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 텍스트 인식 작업 상태 정보
 * 메모리에서 작업 상태를 관리하기 위한 내부 DTO
 */
public record JobState(
    String jobId,
    JobStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
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
            JobStatus.SUBMITTED,
            now,
            now,
            originalFilename,
            fileSize,
            callbackUrl,
            null,
            null,
            null
        );
    }
    
    /**
     * 상태 업데이트
     */
    public JobState updateStatus(JobStatus newStatus) {
        return new JobState(
            jobId,
            newStatus,
            createdAt,
            LocalDateTime.now(),
            originalFilename,
            fileSize,
            callbackUrl,
            answers,
            metadata,
            errorMessage
        );
    }
    
    /**
     * 완료 처리
     */
    public JobState complete(List<AnswerDto> answers, MetadataDto metadata) {
        return new JobState(
            jobId,
            JobStatus.COMPLETED,
            createdAt,
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
            JobStatus.FAILED,
            createdAt,
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
}