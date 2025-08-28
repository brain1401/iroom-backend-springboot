package com.iroomclass.springbackend.domain.admin.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 상세 조회 응답 DTO
 * 
 * 특정 문제의 상세 정보 조회 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailResponse {
    
    private Long questionId;
    private Long unitId;
    private String unitName;
    private String difficulty;
    private String stem;        // 문제 내용 (HTML)
    private String answerKey;   // 정답
}
