package com.iroomclass.springbackend.domain.exam.dto.result;

import com.iroomclass.springbackend.common.BaseRecord;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult;
import com.iroomclass.springbackend.domain.exam.entity.ExamResult.ResultStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI 채점 시험 결과 응답 DTO
 * 
 * @param id              시험 결과 ID
 * @param submissionId    시험 제출 ID
 * @param gradedAt        채점일시
 * @param totalScore      총점
 * @param status          채점 상태
 * @param scoringComment  채점 코멘트
 * @param version         재채점 버전
 * @param gradingProgress 채점 진행률
 * @param isAutoGrading   AI 자동 채점 여부 (항상 true)
 * @param isCompleted     채점 완료 여부
 * @param isRegraded      재채점 여부
 * @param questionResults 문제별 채점 결과 목록
 * @param createdAt       생성일시
 * @param updatedAt       수정일시
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(name = "ExamResultResponse", description = "AI 채점 시험 결과 응답")
public record ExamResultResponse(
        @Schema(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000") 
        UUID id,

        @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001") 
        UUID submissionId,

        @Schema(description = "채점일시", example = "2025-08-17T14:30:00") 
        LocalDateTime gradedAt,

        @Schema(description = "총점", example = "85") 
        Integer totalScore,

        @Schema(description = "채점 상태", example = "COMPLETED") 
        ResultStatus status,

        @Schema(description = "채점 코멘트", example = "전체적으로 잘 답변했습니다.") 
        String scoringComment,

        @Schema(description = "재채점 버전", example = "1") 
        Integer version,

        @Schema(description = "채점 진행률", example = "1.0") 
        BigDecimal gradingProgress,

        @Schema(description = "AI 자동 채점 여부 (항상 true)", example = "true") 
        Boolean isAutoGrading,

        @Schema(description = "채점 완료 여부", example = "true") 
        Boolean isCompleted,

        @Schema(description = "재채점 여부", example = "false") 
        Boolean isRegraded,

        @Schema(description = "문제별 채점 결과 목록") 
        List<QuestionResultResponse> questionResults,

        @Schema(description = "생성일시", example = "2025-08-17T14:00:00") 
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2025-08-17T14:30:00") 
        LocalDateTime updatedAt) implements BaseRecord {

    public ExamResultResponse {
        requireNonNull(id, "id");
        requireNonNull(submissionId, "submissionId");
        requireNonNull(status, "status");
        requireNonNull(version, "version");
        requireNonNull(isAutoGrading, "isAutoGrading");
        requireNonNull(isCompleted, "isCompleted");
        requireNonNull(isRegraded, "isRegraded");
        requireNonNull(createdAt, "createdAt");
    }

    /**
     * Entity에서 DTO로 변환
     * 
     * @param entity ExamResult 엔티티
     * @return ExamResultResponse
     */
    public static ExamResultResponse from(ExamResult entity) {
        return new ExamResultResponse(
                entity.getId(),
                entity.getExamSubmission().getId(),
                entity.getGradedAt(),
                entity.getTotalScore(),
                entity.getStatus(),
                entity.getScoringComment(),
                entity.getVersion(),
                entity.getGradingProgress(),
                entity.isAutoGrading(),
                entity.isCompleted(),
                entity.isRegraded(),
                entity.getQuestionResults().stream()
                        .map(QuestionResultResponse::from)
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Entity에서 DTO로 변환 (문제별 결과 제외)
     * 
     * @param entity ExamResult 엔티티
     * @return ExamResultResponse
     */
    public static ExamResultResponse fromWithoutQuestions(ExamResult entity) {
        return new ExamResultResponse(
                entity.getId(),
                entity.getExamSubmission().getId(),
                entity.getGradedAt(),
                entity.getTotalScore(),
                entity.getStatus(),
                entity.getScoringComment(),
                entity.getVersion(),
                entity.getGradingProgress(),
                entity.isAutoGrading(),
                entity.isCompleted(),
                entity.isRegraded(),
                null, // 문제별 결과 제외
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}