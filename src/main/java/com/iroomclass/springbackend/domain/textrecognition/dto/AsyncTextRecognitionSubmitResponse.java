package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 비동기 글자인식 제출 응답 DTO
 */
@Builder
@Schema(description = "비동기 글자인식 제출 응답")
public record AsyncTextRecognitionSubmitResponse(
    @Schema(description = "작업 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String jobId,
    
    @Schema(description = "작업 상태", example = "submitted")
    String status,
    
    @Schema(description = "예상 완료 시간", example = "2025-01-15T10:32:00")
    LocalDateTime estimatedCompletionTime,
    
    @Schema(description = "등록한 콜백 URL", example = "http://localhost:3055/api/text-recognition/callback/123")
    String callbackUrl,
    
    @Schema(description = "제출 시간", example = "2025-01-15T10:30:00")
    LocalDateTime submittedAt
) {}