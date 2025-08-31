package com.iroomclass.springbackend.domain.admin.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import java.util.List;

/**
 * 시험지 문제 관리 응답 DTO
 * 
 * 문제 추가/제거/교체 후 현재 시험지의 문제 구성 상태를 반환합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record ExamSheetQuestionManageResponse(
    
    @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID examSheetId,
    
    @Schema(description = "시험지 이름", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "현재 문제 수", example = "15")
    Integer currentQuestionCount,
    
    @Schema(description = "목표 문제 수", example = "20")
    Integer targetQuestionCount,
    
    @Schema(description = "객관식 문제 수", example = "10")
    Integer multipleChoiceCount,
    
    @Schema(description = "주관식 문제 수", example = "5")
    Integer subjectiveCount,
    
    @Schema(description = "현재 총 배점", example = "85")
    Integer totalPoints,
    
    @Schema(description = "시험지에 포함된 문제 목록")
    List<QuestionInExamSheet> questions
) {
    
    /**
     * 시험지에 포함된 문제 정보
     */
    public record QuestionInExamSheet(
        

        
        @Schema(description = "문제 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID questionId,
        
        @Schema(description = "단원 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID unitId,
        
        @Schema(description = "단원명", example = "정수와 유리수")
        String unitName,
        
        @Schema(description = "문제 유형", example = "SUBJECTIVE")
        String questionType,
        
        @Schema(description = "난이도", example = "중")
        String difficulty,
        
        @Schema(description = "배점", example = "5")
        Integer points,
        
        @Schema(description = "선택 방식", example = "MANUAL")
        String selectionMethod
    ) {}
}