package com.iroomclass.springbackend.domain.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI 채점 결과 DTO
 * 
 * <p>AI 서버로부터 받은 채점 결과를 담는 DTO입니다.</p>
 */
@Schema(description = "AI 채점 결과")
public record GradingResultDto(
    @Schema(description = "채점 결과 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("result_id")
    UUID resultId,
    
    @Schema(description = "제출 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("submission_id")
    UUID submissionId,
    
    @Schema(description = "시험지 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("exam_sheet_id")
    UUID examSheetId,
    
    @Schema(description = "채점 상태", example = "COMPLETED", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"})
    String status,
    
    @Schema(description = "총점", example = "85")
    @JsonProperty("total_score")
    Integer totalScore,
    
    @Schema(description = "만점", example = "100")
    @JsonProperty("max_total_score")
    Integer maxTotalScore,
    
    @Schema(description = "문제별 채점 결과")
    @JsonProperty("question_results")
    List<QuestionResultDto> questionResults,
    
    @Schema(description = "채점 메타데이터")
    GradingMetadataDto metadata,
    
    @Schema(description = "전체 채점 코멘트", example = "전반적으로 우수한 답안입니다")
    @JsonProperty("grading_comment")
    String gradingComment,
    
    @Schema(description = "채점 완료 시간")
    @JsonProperty("graded_at")
    LocalDateTime gradedAt,
    
    @Schema(description = "채점 버전", example = "1")
    Integer version
) {
    /**
     * 문제별 채점 결과 DTO
     */
    @Schema(description = "문제별 채점 결과")
    public record QuestionResultDto(
        @Schema(description = "문제 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("question_id")
        UUID questionId,
        
        @Schema(description = "답안 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("answer_id")
        UUID answerId,
        
        @Schema(description = "정답 여부", example = "true")
        @JsonProperty("is_correct")
        Boolean isCorrect,
        
        @Schema(description = "획득 점수", example = "5")
        Integer score,
        
        @Schema(description = "배점", example = "5")
        @JsonProperty("max_score")
        Integer maxScore,
        
        @Schema(description = "채점 방식", example = "AUTO", allowableValues = {"AUTO", "MANUAL", "AI"})
        @JsonProperty("grading_method")
        String gradingMethod,
        
        @Schema(description = "신뢰도 점수", example = "0.95")
        @JsonProperty("confidence_score")
        String confidenceScore,
        
        @Schema(description = "문제별 채점 코멘트", example = "정답입니다")
        @JsonProperty("scoring_comment")
        String scoringComment,
        
        @Schema(description = "생성 시간")
        @JsonProperty("created_at")
        LocalDateTime createdAt
    ) {}
    
    /**
     * 채점 메타데이터 DTO
     */
    @Schema(description = "채점 메타데이터")
    public record GradingMetadataDto(
        @Schema(description = "총 문제 수", example = "20")
        @JsonProperty("total_questions")
        Integer totalQuestions,
        
        @Schema(description = "객관식 문제 수", example = "15")
        @JsonProperty("multiple_choice_count")
        Integer multipleChoiceCount,
        
        @Schema(description = "주관식 문제 수", example = "5")
        @JsonProperty("subjective_count")
        Integer subjectiveCount,
        
        @Schema(description = "처리 시간 (밀리초)", example = "1500")
        @JsonProperty("processing_time_ms")
        Long processingTimeMs,
        
        @Schema(description = "AI 모델 버전", example = "gemini-2.5-pro")
        @JsonProperty("ai_model_version")
        String aiModelVersion
    ) {}
}