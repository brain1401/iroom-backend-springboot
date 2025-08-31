package com.iroomclass.springbackend.domain.admin.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import java.util.List;

/**
 * 시험지 미리보기 응답 DTO
 * 
 * 시험지의 전체 구성을 미리보기할 때 사용됩니다.
 * 시험지 정보와 포함된 모든 문제들의 상세 정보를 제공합니다.
 */
@Schema(description = "시험지 미리보기 응답")
public record ExamSheetPreviewResponse(
    
    @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID examSheetId,
    
    @Schema(description = "시험지명", example = "1학년 중간고사")
    String examSheetName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "총 문제 개수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "객관식 문제 개수", example = "15")
    Integer multipleChoiceCount,
    
    @Schema(description = "주관식 문제 개수", example = "5") 
    Integer subjectiveCount,
    
    @Schema(description = "총 배점", example = "100")
    Integer totalPoints,
    
    @Schema(description = "시험지에 포함된 문제 목록")
    List<ExamSheetQuestionPreview> questions,
    
    @Schema(description = "문제 타입별 통계")
    QuestionTypeStatistics statistics
    
) {
    
    /**
     * 시험지 문제 미리보기
     */
    @Schema(description = "시험지 문제 미리보기")
    public record ExamSheetQuestionPreview(
        
        @Schema(description = "문제 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID questionId,
        

        
        @Schema(description = "문제 배점", example = "5")
        Integer points,
        
        @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE")
        String questionType,
        
        @Schema(description = "문제 난이도", example = "EASY")
        String difficulty,
        
        @Schema(description = "단원 이름", example = "1. 수와 연산")
        String unitName,
        
        @Schema(description = "문제 내용 (요약)", example = "다음 중 올바른 계산 결과는?")
        String questionSummary,
        
        @Schema(description = "선택 방식", example = "MANUAL")
        String selectionMethod
        
    ) {}
    
    /**
     * 문제 타입별 통계
     */
    @Schema(description = "문제 타입별 통계")
    public record QuestionTypeStatistics(
        
        @Schema(description = "객관식 문제 배점 합계", example = "75")
        Integer multipleChoicePoints,
        
        @Schema(description = "주관식 문제 배점 합계", example = "25")
        Integer subjectivePoints,
        
        @Schema(description = "쉬운 문제 개수", example = "8")
        Integer easyCount,
        
        @Schema(description = "보통 문제 개수", example = "10")
        Integer mediumCount,
        
        @Schema(description = "어려운 문제 개수", example = "2") 
        Integer hardCount,
        
        @Schema(description = "단원별 문제 분포")
        List<UnitDistribution> unitDistributions
        
    ) {}
    
    /**
     * 단원별 문제 분포
     */
    @Schema(description = "단원별 문제 분포")
    public record UnitDistribution(
        
        @Schema(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID unitId,
        
        @Schema(description = "단원 이름", example = "1. 수와 연산")
        String unitName,
        
        @Schema(description = "해당 단원 문제 개수", example = "5")
        Integer questionCount,
        
        @Schema(description = "해당 단원 총 배점", example = "25")
        Integer totalPoints
        
    ) {}
}