package com.iroomclass.springbackend.domain.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 통계 조회 응답 DTO
 * 
 * 단원별 문제 통계 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatisticsResponse {
    
    private Long unitId;
    private String unitName;
    private int totalQuestions;
    private int easyCount;
    private int mediumCount;
    private int hardCount;
}
