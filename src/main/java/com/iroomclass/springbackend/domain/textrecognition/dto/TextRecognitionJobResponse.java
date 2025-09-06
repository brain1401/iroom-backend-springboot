package com.iroomclass.springbackend.domain.textrecognition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 텍스트 인식 작업 응답 DTO
 */
@Schema(description = "텍스트 인식 작업 생성 응답")
public record TextRecognitionJobResponse(
        
    @Schema(description = "작업 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
    String jobId,
    
    @Schema(description = "작업 상태", example = "SUBMITTED", allowableValues = {"SUBMITTED", "PROCESSING", "COMPLETED", "FAILED"})
    String status,
    
    @Schema(description = "작업 생성 시간", example = "2025-09-06T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @Schema(description = "SSE 연결 URL", example = "/api/text-recognition/sse/550e8400-e29b-41d4-a716-446655440000")
    String sseUrl
    
) {
    /**
     * 성공적인 작업 생성 응답 생성
     */
    public static TextRecognitionJobResponse success(String jobId) {
        return new TextRecognitionJobResponse(
            jobId,
            "SUBMITTED", 
            LocalDateTime.now(),
            "/api/text-recognition/sse/" + jobId
        );
    }
}