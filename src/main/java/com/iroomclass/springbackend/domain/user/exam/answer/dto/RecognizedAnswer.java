package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI가 인식한 답안 정보
 * 
 * AI 이미지 인식 서비스에서 반환하는 답안 정보를 담습니다.
 */
@Schema(description = "AI가 인식한 답안 정보")
public record RecognizedAnswer(
    @Schema(description = "문제 번호", example = "1")
    Integer questionNumber,
    
    @Schema(description = "AI가 인식한 답안 텍스트", example = "x = 5")
    String recognizedAnswer,
    
    @Schema(description = "인식 신뢰도 (0.0 ~ 1.0)", example = "0.95")
    Double confidenceScore
) {
    public RecognizedAnswer {
        Objects.requireNonNull(questionNumber, "questionNumber은 필수입니다");
        Objects.requireNonNull(recognizedAnswer, "recognizedAnswer은 필수입니다");
        Objects.requireNonNull(confidenceScore, "confidenceScore는 필수입니다");
    }
}