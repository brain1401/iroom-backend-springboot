package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 비동기 글자인식 콜백 데이터 DTO
 * AI 서버에서 Spring Boot로 전송하는 콜백 데이터
 */
@Builder
@Schema(description = "비동기 글자인식 콜백 데이터")
public record AsyncTextRecognitionCallbackData(
    @Schema(description = "작업 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String jobId,
    
    @Schema(description = "작업 상태", allowableValues = {"completed", "failed"})
    String status,
    
    @Schema(description = "글자인식 결과 (completed 시)")
    TextRecognitionAnswerResponse result,
    
    @Schema(description = "에러 정보 (failed 시)")
    ErrorInfo error,
    
    @Schema(description = "처리 시간 (밀리초)", example = "45000")
    Long processingTimeMs,
    
    @Schema(description = "완료 시간", example = "2025-01-15T10:32:00")
    LocalDateTime completedAt
) {
    /**
     * 에러 정보
     */
    @Builder
    public record ErrorInfo(
        @Schema(description = "에러 코드", example = "IMAGE_VALIDATION_FAILED")
        String errorCode,
        
        @Schema(description = "에러 메시지", example = "이미지 형식이 올바르지 않습니다")
        String errorMessage,
        
        @Schema(description = "상세 정보")
        String details
    ) {}
}