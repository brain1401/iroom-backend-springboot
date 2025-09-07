package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 배치 글자인식 요청 DTO
 */
@Builder
@Schema(description = "배치 글자인식 요청")
public record BatchTextRecognitionRequest(
    @Schema(description = "이미지 파일 배열 (최대 20개)")
    List<MultipartFile> files,
    
    @Schema(description = "우선순위 (1-5)", example = "1", minimum = "1", maximum = "5")
    Integer priority,
    
    @Schema(description = "캐시 사용 여부", example = "true")
    Boolean useCache
) {
    public BatchTextRecognitionRequest {
        // 기본값 설정
        if (priority == null) priority = 1;
        if (useCache == null) useCache = true;
        
        // 유효성 검증
        if (files != null && files.size() > 20) {
            throw new IllegalArgumentException("파일 개수는 최대 20개까지 가능합니다");
        }
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("우선순위는 1-5 사이여야 합니다");
        }
    }
}