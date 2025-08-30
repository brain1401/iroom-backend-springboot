package com.iroomclass.springbackend.domain.user.exam.result.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult;
import com.iroomclass.springbackend.domain.user.exam.result.entity.ExamResult.ResultStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 결과 응답 DTO
 * 
 * @param id 시험 결과 ID
 * @param submissionId 시험 제출 ID
 * @param gradedBy 채점자 정보
 * @param gradedAt 채점일시
 * @param totalScore 총점
 * @param status 채점 상태
 * @param gradingComment 채점 코멘트
 * @param version 재채점 버전
 * @param gradingProgress 채점 진행률
 * @param isAutoGrading 자동 채점 여부
 * @param isCompleted 채점 완료 여부
 * @param isRegraded 재채점 여부
 * @param questionResults 문제별 채점 결과 목록
 * @param createdAt 생성일시
 * @param updatedAt 수정일시
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record ExamResultDto(
    @Schema(description = "시험 결과 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID submissionId,
    
    @Schema(description = "채점자 정보")
    GraderInfo gradedBy,
    
    @Schema(description = "채점일시", example = "2025-08-17T14:30:00")
    LocalDateTime gradedAt,
    
    @Schema(description = "총점", example = "85")
    Integer totalScore,
    
    @Schema(description = "채점 상태", example = "COMPLETED")
    ResultStatus status,
    
    @Schema(description = "채점 코멘트", example = "전체적으로 잘 답변했습니다.")
    String gradingComment,
    
    @Schema(description = "재채점 버전", example = "1")
    Integer version,
    
    @Schema(description = "채점 진행률", example = "1.0")
    BigDecimal gradingProgress,
    
    @Schema(description = "자동 채점 여부", example = "false")
    Boolean isAutoGrading,
    
    @Schema(description = "채점 완료 여부", example = "true")
    Boolean isCompleted,
    
    @Schema(description = "재채점 여부", example = "false")
    Boolean isRegraded,
    
    @Schema(description = "문제별 채점 결과 목록")
    List<QuestionResultDto> questionResults,
    
    @Schema(description = "생성일시", example = "2025-08-17T14:00:00")
    LocalDateTime createdAt,
    
    @Schema(description = "수정일시", example = "2025-08-17T14:30:00")
    LocalDateTime updatedAt
) {
    
    /**
     * Entity에서 DTO로 변환
     * 
     * @param entity ExamResult 엔티티
     * @return ExamResultDto
     */
    public static ExamResultDto from(ExamResult entity) {
        return new ExamResultDto(
            entity.getId(),
            entity.getExamSubmission().getId(),
            entity.getGradedBy() != null ? GraderInfo.from(entity.getGradedBy()) : null,
            entity.getGradedAt(),
            entity.getTotalScore(),
            entity.getStatus(),
            entity.getGradingComment(),
            entity.getVersion(),
            entity.getGradingProgress(),
            entity.isAutoGrading(),
            entity.isCompleted(),
            entity.isRegraded(),
            entity.getQuestionResults().stream()
                .map(QuestionResultDto::from)
                .toList(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    /**
     * Entity에서 DTO로 변환 (문제별 결과 제외)
     * 
     * @param entity ExamResult 엔티티
     * @return ExamResultDto
     */
    public static ExamResultDto fromWithoutQuestions(ExamResult entity) {
        return new ExamResultDto(
            entity.getId(),
            entity.getExamSubmission().getId(),
            entity.getGradedBy() != null ? GraderInfo.from(entity.getGradedBy()) : null,
            entity.getGradedAt(),
            entity.getTotalScore(),
            entity.getStatus(),
            entity.getGradingComment(),
            entity.getVersion(),
            entity.getGradingProgress(),
            entity.isAutoGrading(),
            entity.isCompleted(),
            entity.isRegraded(),
            null, // 문제별 결과 제외
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    /**
     * 채점자 정보 내부 클래스
     */
    public record GraderInfo(
        @Schema(description = "채점자 ID", example = "123e4567-e89b-12d3-a456-426614174002")
        UUID id,
        
        @Schema(description = "채점자 이름", example = "김선생")
        String name,
        
        @Schema(description = "채점자 이메일", example = "teacher@school.com")
        String email
    ) {
        
        /**
         * Admin Entity에서 GraderInfo로 변환
         * 
         * @param admin Admin 엔티티
         * @return GraderInfo
         */
        public static GraderInfo from(com.iroomclass.springbackend.domain.admin.info.entity.Admin admin) {
            return new GraderInfo(
                admin.getId(),
                admin.getName(),
                admin.getEmail()
            );
        }
    }
}