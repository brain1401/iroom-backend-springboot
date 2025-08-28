package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI가 인식한 답안 정보
 * 
 * AI 이미지 인식 서비스에서 반환하는 답안 정보를 담습니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognizedAnswer {

    /**
     * 문제 번호
     */
    private Integer questionNumber;

    /**
     * AI가 인식한 답안 텍스트
     */
    private String recognizedAnswer;

    /**
     * 인식 신뢰도 (0.0 ~ 1.0)
     */
    private Double confidenceScore;
}
