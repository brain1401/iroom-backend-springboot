package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 배치 진행률 이벤트 DTO (SSE용)
 */
@Builder
@Schema(description = "배치 진행률 이벤트")
public record BatchProgressEvent(
    @Schema(description = "배치 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String batchId,
    
    @Schema(description = "진행률 (퍼센트)", example = "45.5")
    Double progressPercentage,
    
    @Schema(description = "완료된 항목 수", example = "9")
    Integer completedItems,
    
    @Schema(description = "실패한 항목 수", example = "1")
    Integer failedItems,
    
    @Schema(description = "전체 항목 수", example = "20")
    Integer totalItems,
    
    @Schema(description = "상태", allowableValues = {"processing", "completed", "failed"})
    String status
) {}