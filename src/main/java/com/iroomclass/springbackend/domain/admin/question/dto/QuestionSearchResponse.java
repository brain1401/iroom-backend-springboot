package com.iroomclass.springbackend.domain.admin.question.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 검색 응답 DTO
 * 
 * 키워드로 문제 검색 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSearchResponse {
    
    private String keyword;
    private List<QuestionInfo> questions;
    private int totalResults;
    
    /**
     * 검색된 문제 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private Long questionId;
        private Long unitId;
        private String unitName;
        private String difficulty;
        private String stem;  // 문제 내용 (HTML)
    }
}
