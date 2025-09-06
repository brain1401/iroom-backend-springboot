package com.iroomclass.springbackend.domain.textrecognition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SSE로 전송할 텍스트 인식 이벤트 DTO
 */
@Schema(description = "SSE 이벤트 데이터")
public record TextRecognitionSseEvent(
        
    @Schema(description = "이벤트 타입", example = "STATUS_CHANGE", allowableValues = {"STATUS_CHANGE", "COMPLETED", "FAILED"})
    String eventType,
    
    @Schema(description = "작업 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000") 
    String jobId,
    
    @Schema(description = "작업 상태", example = "PROCESSING")
    String status,
    
    @Schema(description = "이벤트 발생 시간", example = "2025-09-06T10:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    
    @Schema(description = "메시지", example = "글자인식 처리 중입니다")
    String message,
    
    @Schema(description = "인식된 답안 목록 (완료 시만)")
    List<AnswerDto> answers,
    
    @Schema(description = "처리 메타데이터 (완료 시만)")
    MetadataDto metadata,
    
    @Schema(description = "오류 메시지 (실패 시만)")
    String errorMessage
    
) {
    
    /**
     * 상태 변경 이벤트 생성
     */
    public static TextRecognitionSseEvent statusChange(String jobId, JobStatus status, String message) {
        return new TextRecognitionSseEvent(
            "STATUS_CHANGE",
            jobId,
            status.name(),
            LocalDateTime.now(),
            message,
            null,
            null,
            null
        );
    }
    
    /**
     * 완료 이벤트 생성
     */
    public static TextRecognitionSseEvent completed(String jobId, List<AnswerDto> answers, MetadataDto metadata) {
        return new TextRecognitionSseEvent(
            "COMPLETED",
            jobId,
            JobStatus.COMPLETED.name(),
            LocalDateTime.now(),
            "글자인식이 성공적으로 완료되었습니다",
            answers,
            metadata,
            null
        );
    }
    
    /**
     * 실패 이벤트 생성
     */
    public static TextRecognitionSseEvent failed(String jobId, String errorMessage) {
        return new TextRecognitionSseEvent(
            "FAILED",
            jobId,
            JobStatus.FAILED.name(),
            LocalDateTime.now(),
            "글자인식 처리에 실패했습니다",
            null,
            null,
            errorMessage
        );
    }
}