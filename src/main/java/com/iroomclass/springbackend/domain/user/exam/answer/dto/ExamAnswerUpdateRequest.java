package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 답안 수정 요청 DTO
 * 
 * 학생이 AI 인식 결과를 수정할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerUpdateRequest {
    
    /**
     * 답안 ID
     */
    @NotNull(message = "답안 ID는 필수입니다.")
    private Long answerId;
    
    /**
     * 수정된 답안 텍스트
     */
    @NotBlank(message = "답안 내용은 필수입니다.")
    private String answerText;
}
