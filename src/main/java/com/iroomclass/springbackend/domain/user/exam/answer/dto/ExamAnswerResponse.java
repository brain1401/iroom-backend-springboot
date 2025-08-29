package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * 시험 답안 응답 DTO
 * 
 * 주관식과 객관식 답안 정보를 클라이언트에 전달할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 답안 응답")
public record ExamAnswerResponse(
    @Schema(description = "답안 ID", example = "1")
    Long answerId,
    
    @Schema(description = "시험 제출 ID", example = "1")
    Long examSubmissionId,
    
    @Schema(description = "문제 ID", example = "3")
    Long questionId,
    
    @Schema(description = "답안 이미지 URL (주관식 문제용)", example = "/uploads/answers/answer_1.jpg", nullable = true)
    String answerImageUrl,
    
    @Schema(description = "AI 인식 결과 (주관식 문제용)", example = "x = 5", nullable = true)
    String answerText,
    
    @Schema(description = "선택한 답안 번호 (객관식 문제용)", example = "2", nullable = true)
    Integer selectedChoice,
    
    @Schema(description = "정답 여부", example = "true", nullable = true)
    Boolean isCorrect,
    
    @Schema(description = "획득 점수", example = "10", nullable = true)
    Integer score
) {
    public ExamAnswerResponse {
        Objects.requireNonNull(answerId, "answerId은 필수입니다");
        Objects.requireNonNull(examSubmissionId, "examSubmissionId은 필수입니다");
        Objects.requireNonNull(questionId, "questionId은 필수입니다");
        
        // 주관식과 객관식 중 하나의 답안 정보는 있어야 함
        if (answerImageUrl == null && selectedChoice == null) {
            throw new IllegalArgumentException("주관식 답안 이미지 또는 객관식 선택 답안 중 하나는 필수입니다");
        }
    }
    
    /**
     * Entity를 DTO로 변환하는 정적 메서드
     */
    public static ExamAnswerResponse from(ExamAnswer examAnswer) {
        return new ExamAnswerResponse(
            examAnswer.getId(),
            examAnswer.getExamSubmission().getId(),
            examAnswer.getQuestion().getId(),
            examAnswer.getAnswerImageUrl(),
            examAnswer.getAnswerText(),
            examAnswer.getSelectedChoice(),
            examAnswer.getIsCorrect(),
            examAnswer.getScore()
        );
    }
}