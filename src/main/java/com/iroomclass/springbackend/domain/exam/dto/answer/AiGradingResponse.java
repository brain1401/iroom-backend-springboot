package com.iroomclass.springbackend.domain.exam.dto.answer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 서버 채점 응답 DTO
 * 
 * AI 서버의 /grading/{submission_id} API 응답을 매핑합니다.
 */
@Schema(description = "AI 서버 채점 응답")
public record AiGradingResponse(
    @Schema(description = "결과 ID")
    @JsonProperty("result_id")
    UUID resultId,
    
    @Schema(description = "제출 ID")
    @JsonProperty("submission_id")
    UUID submissionId,
    
    @Schema(description = "시험지 ID")
    @JsonProperty("exam_sheet_id")
    UUID examSheetId,
    
    @Schema(description = "처리 상태")
    @JsonProperty("status")
    GradingStatus status,
    
    @Schema(description = "총 점수")
    @JsonProperty("total_score")
    Double totalScore,
    
    @Schema(description = "최대 총 점수")
    @JsonProperty("max_total_score")
    Double maxTotalScore,
    
    @Schema(description = "문제별 결과")
    @JsonProperty("question_results")
    List<AiQuestionResult> questionResults,
    
    @Schema(description = "메타데이터")
    @JsonProperty("metadata")
    AiGradingMetadata metadata,
    
    @Schema(description = "채점 코멘트")
    @JsonProperty("grading_comment")
    String gradingComment,
    
    @Schema(description = "채점 완료 시간")
    @JsonProperty("graded_at")
    LocalDateTime gradedAt,
    
    @Schema(description = "버전")
    @JsonProperty("version")
    Integer version
) {
    public AiGradingResponse {
        Objects.requireNonNull(resultId, "resultId는 필수입니다");
        Objects.requireNonNull(submissionId, "submissionId는 필수입니다");
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
        Objects.requireNonNull(status, "status는 필수입니다");
        Objects.requireNonNull(totalScore, "totalScore는 필수입니다");
        Objects.requireNonNull(maxTotalScore, "maxTotalScore는 필수입니다");
        Objects.requireNonNull(questionResults, "questionResults는 필수입니다");
        Objects.requireNonNull(metadata, "metadata는 필수입니다");
        Objects.requireNonNull(version, "version는 필수입니다");
    }
    
    /**
     * 채점 상태 열거형
     */
    public enum GradingStatus {
        PENDING, COMPLETED, FAILED
    }
    
    /**
     * AI 문제별 채점 결과
     */
    @Schema(description = "AI 문제별 채점 결과")
    public record AiQuestionResult(
        @Schema(description = "문제 ID")
        @JsonProperty("question_id")
        UUID questionId,
        
        @Schema(description = "답안 ID")
        @JsonProperty("answer_id")
        UUID answerId,
        
        @Schema(description = "정답 여부")
        @JsonProperty("is_correct")
        Boolean isCorrect,
        
        @Schema(description = "획득 점수")
        @JsonProperty("score")
        Double score,
        
        @Schema(description = "최대 점수")
        @JsonProperty("max_score")
        Double maxScore,
        
        @Schema(description = "채점 방식")
        @JsonProperty("grading_method")
        String gradingMethod,
        
        @Schema(description = "신뢰도 점수")
        @JsonProperty("confidence_score")
        String confidenceScore,
        
        @Schema(description = "채점 코멘트")
        @JsonProperty("grading_comment")
        String gradingComment,
        
        @Schema(description = "생성 시간")
        @JsonProperty("created_at")
        LocalDateTime createdAt
    ) {
        public AiQuestionResult {
            Objects.requireNonNull(questionId, "questionId는 필수입니다");
            Objects.requireNonNull(answerId, "answerId는 필수입니다");
            Objects.requireNonNull(isCorrect, "isCorrect는 필수입니다");
            Objects.requireNonNull(score, "score는 필수입니다");
            Objects.requireNonNull(maxScore, "maxScore는 필수입니다");
            Objects.requireNonNull(gradingMethod, "gradingMethod는 필수입니다");
            Objects.requireNonNull(createdAt, "createdAt는 필수입니다");
        }
    }
    
    /**
     * AI 채점 메타데이터
     */
    @Schema(description = "AI 채점 메타데이터")
    public record AiGradingMetadata(
        @Schema(description = "총 문제 수")
        @JsonProperty("total_questions")
        Integer totalQuestions,
        
        @Schema(description = "객관식 문제 수")
        @JsonProperty("multiple_choice_count")
        Integer multipleChoiceCount,
        
        @Schema(description = "주관식 문제 수")
        @JsonProperty("subjective_count")
        Integer subjectiveCount,
        
        @Schema(description = "처리 시간 (밀리초)")
        @JsonProperty("processing_time_ms")
        Integer processingTimeMs,
        
        @Schema(description = "AI 모델 버전")
        @JsonProperty("ai_model_version")
        String aiModelVersion
    ) {
        public AiGradingMetadata {
            Objects.requireNonNull(totalQuestions, "totalQuestions는 필수입니다");
            Objects.requireNonNull(multipleChoiceCount, "multipleChoiceCount는 필수입니다");
            Objects.requireNonNull(subjectiveCount, "subjectiveCount는 필수입니다");
            Objects.requireNonNull(processingTimeMs, "processingTimeMs는 필수입니다");
            Objects.requireNonNull(aiModelVersion, "aiModelVersion는 필수입니다");
        }
    }
}