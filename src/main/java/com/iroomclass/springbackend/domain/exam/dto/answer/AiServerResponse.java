package com.iroomclass.springbackend.domain.exam.dto.answer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 서버 글자인식 응답 DTO
 * 
 * AI 서버의 /text-recognition/answer-sheet API 응답을 매핑합니다.
 */
@Schema(description = "AI 서버 글자인식 응답")
public record AiServerResponse(
    @Schema(description = "답안지 고유 ID")
    @JsonProperty("sheet_id")
    String sheetId,
    
    @Schema(description = "처리 타임스탬프")
    @JsonProperty("processing_timestamp")
    LocalDateTime processingTimestamp,
    
    @Schema(description = "인식된 답안 목록")
    @JsonProperty("answers")
    List<AiRecognizedAnswer> answers,
    
    @Schema(description = "메타데이터")
    @JsonProperty("metadata")
    AiMetadata metadata
) {
    public AiServerResponse {
        Objects.requireNonNull(sheetId, "sheetId는 필수입니다");
        Objects.requireNonNull(processingTimestamp, "processingTimestamp는 필수입니다");
        Objects.requireNonNull(answers, "answers는 필수입니다");
        Objects.requireNonNull(metadata, "metadata는 필수입니다");
    }
    
    /**
     * AI가 인식한 개별 답안
     */
    @Schema(description = "AI가 인식한 개별 답안")
    public record AiRecognizedAnswer(
        @Schema(description = "문제 번호")
        @JsonProperty("question_number") 
        Integer questionNumber,
        
        @Schema(description = "문제 라벨")
        @JsonProperty("question_label") 
        String questionLabel,
        
        @Schema(description = "추출된 텍스트")
        @JsonProperty("extracted_text") 
        String extractedText,
        
        @Schema(description = "LaTeX 수식")
        @JsonProperty("latex_formula") 
        String latexFormula,
        
        @Schema(description = "신뢰도 (0.0 ~ 1.0)")
        @JsonProperty("confidence") 
        Double confidence
    ) {
        public AiRecognizedAnswer {
            Objects.requireNonNull(questionNumber, "questionNumber는 필수입니다");
            Objects.requireNonNull(extractedText, "extractedText는 필수입니다");
            Objects.requireNonNull(confidence, "confidence는 필수입니다");
        }
        
        /**
         * 기존 RecognizedAnswer로 변환
         * 
         * @return RecognizedAnswer 객체
         */
        public RecognizedAnswer toRecognizedAnswer() {
            // extractedText와 latexFormula 중 더 적절한 것을 선택
            String finalAnswer = (latexFormula != null && !latexFormula.isEmpty()) 
                ? latexFormula 
                : extractedText;
                
            return new RecognizedAnswer(questionNumber, finalAnswer, confidence);
        }
    }
    
    /**
     * AI 서버 응답 메타데이터
     */
    @Schema(description = "AI 서버 응답 메타데이터")
    public record AiMetadata(
        @Schema(description = "이미지 품질")
        @JsonProperty("image_quality") 
        String imageQuality,
        
        @Schema(description = "처리 시간 (밀리초)")
        @JsonProperty("processing_time_ms") 
        Integer processingTimeMs,
        
        @Schema(description = "감지된 총 문제 수")
        @JsonProperty("total_questions_detected") 
        Integer totalQuestionsDetected,
        
        @Schema(description = "AI 모델 버전")
        @JsonProperty("model_version") 
        String modelVersion
    ) {
        public AiMetadata {
            Objects.requireNonNull(imageQuality, "imageQuality는 필수입니다");
            Objects.requireNonNull(processingTimeMs, "processingTimeMs는 필수입니다");
            Objects.requireNonNull(totalQuestionsDetected, "totalQuestionsDetected는 필수입니다");
            Objects.requireNonNull(modelVersion, "modelVersion는 필수입니다");
        }
    }
    
    /**
     * 기존 RecognizedAnswer 리스트로 변환
     * 
     * @return RecognizedAnswer 리스트
     */
    public List<RecognizedAnswer> toRecognizedAnswers() {
        return answers.stream()
            .map(AiRecognizedAnswer::toRecognizedAnswer)
            .toList();
    }
}