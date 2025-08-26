package com.iroomclass.springbackend.domain.question.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 목록 조회 응답 DTO
 * 
 * 단원별 문제 목록 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListResponse {
    
    private Long unitId;
    private String unitName;
    private List<QuestionInfo> questions;
    private int totalQuestions;
    private int easyCount;
    private int mediumCount;
    private int hardCount;
    
    /**
     * 문제 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private Long questionId;
        private String difficulty;
    }
}
