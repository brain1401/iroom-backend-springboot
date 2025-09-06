package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 글자인식 답안 DTO
 */
@Schema(description = "글자인식 답안 정보")
public record AnswerDto(
        
    @Schema(description = "문제 번호", example = "1")
    Integer questionNumber,
    
    @Schema(description = "문제 라벨", example = "92")
    String questionLabel,
    
    @Schema(description = "추출된 텍스트", example = "2x + 3 = 7")
    String extractedText,
    
    @Schema(description = "LaTeX 수식 형태", example = "2x + 3 = 7")
    String latexFormula,
    
    @Schema(description = "인식 신뢰도", example = "0.95", minimum = "0", maximum = "1")
    Double confidence
    
) {
    /**
     * 신뢰도 검증
     */
    public AnswerDto {
        if (confidence != null && (confidence < 0.0 || confidence > 1.0)) {
            throw new IllegalArgumentException("신뢰도는 0.0과 1.0 사이의 값이어야 합니다");
        }
    }
}