package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 답안 목록 응답 DTO
 * 
 * 시험 제출의 모든 답안 정보(주관식과 객관식)를 클라이언트에 전달할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 답안 목록 응답")
public record ExamAnswerListResponse(
    @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID examSubmissionId,
    
    @Schema(description = "답안 목록")
    List<ExamAnswerResponse> answers,
    
    @Schema(description = "총 답안 수", example = "10")
    int totalCount,
    
    @Schema(description = "정답 답안 수", example = "8")
    int correctCount,
    
    @Schema(description = "총 점수", example = "85")
    int totalScore
) {
    public ExamAnswerListResponse {
        Objects.requireNonNull(examSubmissionId, "examSubmissionId은 필수입니다");
        Objects.requireNonNull(answers, "answers은 필수입니다");
    }
    
    /**
     * Entity 리스트를 DTO로 변환하는 정적 메서드
     */
    public static ExamAnswerListResponse from(List<ExamAnswer> examAnswers, UUID examSubmissionId) {
        List<ExamAnswerResponse> answerResponses = examAnswers.stream()
            .map(ExamAnswerResponse::from)
            .toList();
        
        int totalCount = answerResponses.size();
        int correctCount = (int) answerResponses.stream()
            .filter(answer -> Boolean.TRUE.equals(answer.isCorrect()))
            .count();
        int totalScore = answerResponses.stream()
            .mapToInt(answer -> answer.score() != null ? answer.score() : 0)
            .sum();
        
        return new ExamAnswerListResponse(
            examSubmissionId,
            answerResponses,
            totalCount,
            correctCount,
            totalScore
        );
    }
}