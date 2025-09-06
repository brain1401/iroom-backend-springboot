package com.iroomclass.springbackend.domain.textrecognition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 서버로부터 받는 콜백 요청 DTO
 */
@Schema(description = "AI 서버 콜백 요청 데이터")
public record TextRecognitionCallbackRequest(
        
    @NotBlank(message = "작업 ID는 필수입니다")
    @JsonProperty("job_id")
    @Schema(description = "작업 고유 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
    String jobId,
    
    @NotBlank(message = "상태는 필수입니다") 
    @Schema(description = "작업 상태", example = "completed", allowableValues = {"completed", "failed"})
    String status,
    
    @JsonProperty("sheet_id")
    @Schema(description = "답안지 고유 식별자")
    String sheetId,
    
    @JsonProperty("processing_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "처리 완료 시간", example = "2025-09-06T08:03:28.926Z")
    LocalDateTime processingTimestamp,
    
    @Schema(description = "인식된 답안 목록")
    List<AnswerDto> answers,
    
    @Schema(description = "처리 메타데이터")
    MetadataDto metadata,
    
    @JsonProperty("error_message")
    @Schema(description = "오류 메시지 (실패 시만)", example = "이미지 품질이 너무 낮습니다")
    String errorMessage
    
) {
    /**
     * 상태 검증 및 변환
     */
    public JobStatus getJobStatus() {
        return JobStatus.fromString(status);
    }
    
    /**
     * 성공적으로 완료되었는지 확인
     */
    public boolean isSuccessful() {
        return "completed".equalsIgnoreCase(status);
    }
    
    /**
     * 실패했는지 확인
     */
    public boolean isFailed() {
        return "failed".equalsIgnoreCase(status);
    }
    
    /**
     * 오류 메시지가 있는지 확인
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }
}