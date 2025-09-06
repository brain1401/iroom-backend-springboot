package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

/**
 * 텍스트 인식 요청 DTO
 */
@Schema(description = "텍스트 인식 요청")
@Builder
public record TextRecognitionSubmitRequest(
        
    @NotNull(message = "파일은 필수입니다")
    @Schema(description = "답안지 이미지 파일", requiredMode = Schema.RequiredMode.REQUIRED)
    MultipartFile file,
    
    @Schema(description = "캐시 사용 여부", defaultValue = "true")
    Boolean useCache,
    
    @Schema(description = "콘텐츠 해시 사용 여부", defaultValue = "false") 
    Boolean useContentHash,
    
    @Schema(description = "페이지 번호", example = "1", defaultValue = "1")
    Integer pageNumber,
    
    @Schema(description = "질문 유형", example = "단답형", defaultValue = "단답형")
    String questionType,
    
    @Schema(description = "학년 수준", example = "중학교", defaultValue = "중학교")
    String gradeLevel
    
) {
    /**
     * 기본값을 적용한 생성자
     */
    public TextRecognitionSubmitRequest {
        if (useCache == null) {
            useCache = true;
        }
        if (useContentHash == null) {
            useContentHash = false;
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (questionType == null) {
            questionType = "단답형";
        }
        if (gradeLevel == null) {
            gradeLevel = "중학교";
        }
    }
}