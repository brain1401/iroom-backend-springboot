package com.iroomclass.springbackend.domain.textrecognition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 글자인식 결과 응답 DTO
 * AI 서버의 동기식 답안지 인식 결과
 */
@Builder
@Schema(description = "글자인식 결과 응답")
public record TextRecognitionAnswerResponse(
    @JsonProperty("sheet_id")
    @Schema(description = "시트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String sheetId,
    
    @JsonProperty("processing_timestamp")
    @Schema(description = "처리 시간", example = "2025-01-15T10:30:00")
    LocalDateTime processingTimestamp,
    
    @JsonProperty("answers")
    @Schema(description = "답안 목록")
    List<AnswerItem> answers,
    
    @JsonProperty("metadata")
    @Schema(description = "메타데이터")
    Metadata metadata
) {
    
    /**
     * 답안 항목
     */
    @Builder
    public record AnswerItem(
        @JsonProperty("question_number")
        @Schema(description = "문제 번호", example = "1")
        Integer questionNumber,
        
        @JsonProperty("question_label")
        @Schema(description = "문제 라벨", example = "1")
        String questionLabel,
        
        @JsonProperty("solution_process")
        @Schema(description = "풀이 과정")
        TextContent solutionProcess,
        
        @JsonProperty("final_answer")
        @Schema(description = "최종 답안")
        TextContent finalAnswer,
        
        @JsonProperty("confidence")
        @Schema(description = "신뢰도", example = "0.95")
        Double confidence
    ) {}
    
    /**
     * 텍스트 콘텐츠
     */
    @Builder
    public record TextContent(
        @JsonProperty("extracted_text")
        @Schema(description = "추출된 텍스트", example = "풀이과정 텍스트")
        String extractedText,
        
        @JsonProperty("latex_formula")
        @Schema(description = "LaTeX 수식", example = "x^2 + y^2 = z^2")
        String latexFormula
    ) {}
    
    /**
     * 메타데이터
     */
    @Builder
    public record Metadata(
        @JsonProperty("image_quality")
        @Schema(description = "이미지 품질", allowableValues = {"good", "fair", "poor"})
        String imageQuality,
        
        @JsonProperty("processing_time_ms")
        @Schema(description = "처리 시간 (밀리초)", example = "2500")
        Long processingTimeMs,
        
        @JsonProperty("total_questions_detected")
        @Schema(description = "감지된 전체 문제 수", example = "20")
        Integer totalQuestionsDetected,
        
        @JsonProperty("model_version")
        @Schema(description = "모델 버전", example = "gemini-2.5-pro")
        String modelVersion
    ) {}
}