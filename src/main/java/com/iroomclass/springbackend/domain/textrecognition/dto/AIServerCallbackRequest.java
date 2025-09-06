package com.iroomclass.springbackend.domain.textrecognition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * AI 서버로부터의 콜백 요청 DTO
 */
@Schema(name = "AIServerCallbackRequest", description = "AI 서버 콜백 요청 데이터")
@Builder
public record AIServerCallbackRequest(
    
    @NotBlank(message = "작업 ID는 필수입니다")
    @Schema(description = "작업 ID", example = "job_20240817_143052_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    String jobId,
    
    @NotBlank(message = "작업 상태는 필수입니다")
    @Schema(description = "작업 상태", example = "COMPLETED", allowableValues = {"SUBMITTED", "PROCESSING", "COMPLETED", "FAILED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String status,
    
    @Schema(description = "오류 메시지 (실패 시)", example = "파일 처리 중 오류가 발생했습니다")
    String errorMessage,
    
    @Schema(description = "인식된 답안 목록")
    List<RecognizedAnswer> answers,
    
    @Schema(description = "메타데이터 정보")
    String metadata
) {
    
    /**
     * Compact Constructor - 유효성 검증
     */
    public AIServerCallbackRequest {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        
        if (jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be blank");
        }
        if (status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
    }
    
    /**
     * 성공 여부 확인
     */
    public boolean isSuccessful() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * 오류 여부 확인
     */
    public boolean hasError() {
        return "FAILED".equals(status) && errorMessage != null;
    }
    
    /**
     * 메타데이터 반환 (null-safe)
     */
    public String metadata() {
        return metadata != null ? metadata : "{}";
    }
    
    /**
     * RecognizedAnswer를 AnswerDto로 변환
     */
    public List<AnswerDto> convertToAnswerDtos() {
        if (answers == null || answers.isEmpty()) {
            return List.of();
        }
        
        return answers.stream()
            .map(recognizedAnswer -> new AnswerDto(
                recognizedAnswer.questionNumber(),
                recognizedAnswer.questionNumber() != null ? recognizedAnswer.questionNumber().toString() : "N/A",
                recognizedAnswer.recognizedText(),
                recognizedAnswer.recognizedText(), // LaTeX 처리는 향후 구현
                recognizedAnswer.confidence()
            ))
            .toList();
    }
    
    /**
     * metadata JSON 문자열을 MetadataDto로 변환
     */
    public MetadataDto convertToMetadataDto() {
        if (metadata == null || metadata.isBlank() || "{}".equals(metadata)) {
            // 기본 메타데이터 반환
            return new MetadataDto(
                "MEDIUM",
                null,
                answers != null ? answers.size() : 0,
                "unknown"
            );
        }
        
        try {
            // JSON 파싱 시도
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            var metadataMap = mapper.readValue(metadata, java.util.Map.class);
            
            return new MetadataDto(
                (String) metadataMap.getOrDefault("imageQuality", "MEDIUM"),
                metadataMap.get("processingTimeMs") != null ? 
                    Long.valueOf(metadataMap.get("processingTimeMs").toString()) : null,
                metadataMap.get("totalQuestionsDetected") != null ? 
                    Integer.valueOf(metadataMap.get("totalQuestionsDetected").toString()) : 
                    (answers != null ? answers.size() : 0),
                (String) metadataMap.getOrDefault("modelVersion", "unknown")
            );
        } catch (JsonProcessingException e) {
            // JSON 파싱 실패 시 기본값 사용
            return new MetadataDto(
                "MEDIUM",
                null,
                answers != null ? answers.size() : 0,
                "unknown"
            );
        }
    }
    
    /**
     * 인식된 답안 DTO
     */
    @Schema(name = "RecognizedAnswer", description = "AI가 인식한 답안 정보")
    @Builder
    public record RecognizedAnswer(
        
        @Schema(description = "문제 번호", example = "1")
        Integer questionNumber,
        
        @Schema(description = "인식된 답안 텍스트", example = "2x + 3y = 15")
        String recognizedText,
        
        @Schema(description = "신뢰도 (0.0 ~ 1.0)", example = "0.95")
        Double confidence,
        
        @Schema(description = "답안 영역 좌표 정보")
        BoundingBox boundingBox
    ) {
        
        /**
         * 답안 영역 좌표 DTO
         */
        @Schema(name = "BoundingBox", description = "답안 영역 좌표")
        @Builder
        public record BoundingBox(
            
            @Schema(description = "X 좌표", example = "100")
            Integer x,
            
            @Schema(description = "Y 좌표", example = "200")
            Integer y,
            
            @Schema(description = "너비", example = "300")
            Integer width,
            
            @Schema(description = "높이", example = "50")
            Integer height
        ) {}
    }
}