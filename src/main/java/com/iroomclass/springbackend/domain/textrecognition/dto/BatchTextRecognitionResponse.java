package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 배치 글자인식 응답 DTO
 */
@Builder
@Schema(description = "배치 글자인식 응답")
public record BatchTextRecognitionResponse(
    @Schema(description = "배치 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String batchId,
    
    @Schema(description = "진행률 스트림 URL", example = "/text-recognition/batch/550e8400-e29b-41d4-a716-446655440000/progress")
    String progressStreamUrl,
    
    @Schema(description = "총 항목 수", example = "10")
    Integer totalItems,
    
    @Schema(description = "상태", example = "processing")
    String status
) {}