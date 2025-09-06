package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 글자인식 메타데이터 DTO
 */
@Schema(description = "글자인식 처리 메타데이터")
public record MetadataDto(
        
    @Schema(description = "이미지 품질", example = "HIGH")
    String imageQuality,
    
    @Schema(description = "처리 시간(밀리초)", example = "15000")
    Long processingTimeMs,
    
    @Schema(description = "감지된 총 문제 수", example = "20")
    Integer totalQuestionsDetected,
    
    @Schema(description = "AI 모델 버전", example = "gemini-2.5-pro")
    String modelVersion
    
) {
    /**
     * 처리 시간 검증
     */
    public MetadataDto {
        if (processingTimeMs != null && processingTimeMs < 0) {
            throw new IllegalArgumentException("처리 시간은 음수일 수 없습니다");
        }
        if (totalQuestionsDetected != null && totalQuestionsDetected < 0) {
            throw new IllegalArgumentException("문제 수는 음수일 수 없습니다");
        }
    }
}