package com.iroomclass.springbackend.domain.user.exam.result.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.QuestionResult.GradingMethod;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 문제별 채점 결과 응답 DTO
 * 
 * @param id 문제별 결과 ID
 * @param examResultId 시험 결과 ID
 * @param answerInfo 답안 정보
 * @param isCorrect 정답 여부
 * @param score 획득 점수
 * @param maxScore 만점
 * @param gradingMethod 채점 방법
 * @param confidenceScore 신뢰도 점수
 * @param feedback 피드백
 * @param aiAnalysis AI 분석 결과
 * @param createdAt 생성일시
 * @param updatedAt 수정일시
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record QuestionResultDto(
    @Schema(description = "문제별 결과 ID", example = "123e4567-e89b-12d3-a456-426614174003")
    UUID id,
    
    @Schema(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID examResultId,
    
    @Schema(description = "답안 정보")
    AnswerInfo answerInfo,
    
    @Schema(description = "정답 여부", example = "true")
    Boolean isCorrect,
    
    @Schema(description = "획득 점수", example = "5")
    Integer score,
    
    @Schema(description = "만점", example = "5")
    Integer maxScore,
    
    @Schema(description = "채점 방법", example = "AUTO")
    GradingMethod gradingMethod,
    
    @Schema(description = "신뢰도 점수", example = "0.95")
    BigDecimal confidenceScore,
    
    @Schema(description = "피드백", example = "정답입니다.")
    String feedback,
    
    @Schema(description = "AI 분석 결과", example = "답변이 정확하고 논리적입니다.")
    String aiAnalysis,
    
    @Schema(description = "생성일시", example = "2025-08-17T14:00:00")
    LocalDateTime createdAt,
    
    @Schema(description = "수정일시", example = "2025-08-17T14:30:00")
    LocalDateTime updatedAt
) {
    
    /**
     * Entity에서 DTO로 변환
     * 
     * @param entity QuestionResult 엔티티
     * @return QuestionResultDto
     */
    public static QuestionResultDto from(QuestionResult entity) {
        return new QuestionResultDto(
            entity.getId(),
            entity.getExamResult().getId(),
            AnswerInfo.from(entity.getStudentAnswerSheet()),
            entity.getIsCorrect(),
            entity.getScore(),
            entity.getMaxScore(),
            entity.getGradingMethod(),
            entity.getConfidenceScore(),
            entity.getFeedback(),
            entity.getAiAnalysis(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    /**
     * 답안 정보 내부 클래스
     */
    public record AnswerInfo(
        @Schema(description = "답안 ID", example = "123e4567-e89b-12d3-a456-426614174004")
        UUID id,
        
        @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174005")
        UUID questionId,
        
        @Schema(description = "문제 순서", example = "1")
        Integer questionOrder,
        
        @Schema(description = "제출된 답안", example = "서울")
        String submittedAnswer,
        
        @Schema(description = "답안 타입", example = "TEXT")
        String answerType
    ) {
        
        /**
         * StudentAnswerSheet Entity에서 AnswerInfo로 변환
         * 
         * @param studentAnswerSheet StudentAnswerSheet 엔티티
         * @return AnswerInfo
         */
        public static AnswerInfo from(com.iroomclass.springbackend.domain.user.exam.answer.entity.StudentAnswerSheet studentAnswerSheet) {
            return new AnswerInfo(
                studentAnswerSheet.getId(),
                studentAnswerSheet.getQuestionId(),
                studentAnswerSheet.getQuestionOrder(),
                studentAnswerSheet.getSubmittedAnswer(),
                studentAnswerSheet.getAnswerType().name()
            );
        }
    }
}