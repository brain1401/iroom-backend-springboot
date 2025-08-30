package com.iroomclass.springbackend.domain.admin.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import java.util.List;

/**
 * 선택 가능한 문제 목록 응답 DTO
 * 
 * 시험지에 추가할 수 있는 문제들의 목록을 제공합니다.
 * 단원별, 난이도별, 유형별로 필터링된 결과를 반환합니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
public record SelectableQuestionsResponse(
    
    @Schema(description = "검색 조건 - 단원 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID unitId,
    
    @Schema(description = "검색 조건 - 단원명", example = "정수와 유리수")
    String unitName,
    
    @Schema(description = "검색 조건 - 난이도", example = "중")
    String difficulty,
    
    @Schema(description = "검색 조건 - 문제 유형", example = "SUBJECTIVE")
    String questionType,
    
    @Schema(description = "총 검색 결과 수", example = "15")
    Integer totalCount,
    
    @Schema(description = "선택 가능한 문제 목록")
    List<SelectableQuestion> questions
) {
    
    /**
     * 선택 가능한 문제 정보
     */
    public record SelectableQuestion(
        
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
        
        @Schema(description = "문제 내용 미리보기 (HTML)", example = "<p>다음 수의 절댓값을 구하시오: -5</p>")
        String questionPreview,
        
        @Schema(description = "이미 선택된 문제인지 여부", example = "false")
        Boolean alreadySelected
    ) {}
}