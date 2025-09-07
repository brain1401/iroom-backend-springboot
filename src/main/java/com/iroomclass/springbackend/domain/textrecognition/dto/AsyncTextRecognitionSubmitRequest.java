package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 비동기 글자인식 제출 요청 DTO
 */
@Builder
@Schema(description = "비동기 글자인식 제출 요청")
public record AsyncTextRecognitionSubmitRequest(
    @NotNull(message = "콜백 URL은 필수입니다")
    @Schema(description = "완료 시 결과를 받을 URL", example = "http://localhost:3055/api/text-recognition/callback/{jobId}", required = true)
    String callbackUrl,
    
    @Min(1) @Max(10)
    @Schema(description = "우선순위 (1-10)", example = "5", minimum = "1", maximum = "10")
    Integer priority,
    
    @Schema(description = "캐시 사용 여부", example = "true")
    Boolean useCache
) {
    public AsyncTextRecognitionSubmitRequest {
        // 기본값 설정
        if (priority == null) priority = 5;
        if (useCache == null) useCache = true;
        
        // 유효성 검증
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("우선순위는 1-10 사이여야 합니다");
        }
    }
}