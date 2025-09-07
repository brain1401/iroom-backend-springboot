package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 작업 상태 응답 DTO
 */
@Builder
@Schema(description = "작업 상태 응답")
public record JobStatusResponse(
    @Schema(description = "작업 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String jobId,
    
    @Schema(description = "작업 상태", allowableValues = {"submitted", "processing", "completed", "failed"})
    String status,
    
    @Schema(description = "생성 시간", example = "2025-01-15T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "완료 시간 (nullable)", example = "2025-01-15T10:32:00")
    LocalDateTime completedAt
) {}