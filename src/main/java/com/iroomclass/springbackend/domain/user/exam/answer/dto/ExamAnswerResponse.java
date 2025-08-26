package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 답안 응답 DTO
 * 
 * 답안 정보를 클라이언트에 전달할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시험 답안 응답")
public class ExamAnswerResponse {
    
    @Schema(description = "답안 ID", example = "1")
    private Long answerId;
    
    @Schema(description = "시험 제출 ID", example = "1")
    private Long examSubmissionId;
    
    @Schema(description = "문제 ID", example = "3")
    private Long questionId;
    
    @Schema(description = "답안 이미지 URL", example = "/uploads/answers/answer_1.jpg")
    private String answerImageUrl;
    
    @Schema(description = "AI 인식 결과", example = "x = 5", nullable = true)
    private String answerText;
    
    @Schema(description = "정답 여부", example = "true", nullable = true)
    private Boolean isCorrect;
    
    @Schema(description = "획득 점수", example = "10", nullable = true)
    private Integer score;
    
    /**
     * Entity를 DTO로 변환하는 정적 메서드
     */
    public static ExamAnswerResponse from(ExamAnswer examAnswer) {
        return ExamAnswerResponse.builder()
            .answerId(examAnswer.getId())
            .examSubmissionId(examAnswer.getExamSubmission().getId())
            .questionId(examAnswer.getQuestion().getId())
            .answerImageUrl(examAnswer.getAnswerImageUrl())
            .answerText(examAnswer.getAnswerText())
            .isCorrect(examAnswer.getIsCorrect())
            .score(examAnswer.getScore())
            .build();
    }
}
