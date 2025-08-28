package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import java.util.List;

import com.iroomclass.springbackend.domain.user.exam.answer.entity.ExamAnswer;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 답안 목록 응답 DTO
 * 
 * 시험 제출의 모든 답안 정보를 클라이언트에 전달할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시험 답안 목록 응답")
public class ExamAnswerListResponse {
    
    @Schema(description = "시험 제출 ID", example = "1")
    private Long examSubmissionId;
    
    @Schema(description = "답안 목록")
    private List<ExamAnswerResponse> answers;
    
    @Schema(description = "총 답안 수", example = "10")
    private int totalCount;
    
    @Schema(description = "정답 답안 수", example = "8")
    private int correctCount;
    
    @Schema(description = "총 점수", example = "85")
    private int totalScore;
    
    /**
     * Entity 리스트를 DTO로 변환하는 정적 메서드
     */
    public static ExamAnswerListResponse from(List<ExamAnswer> examAnswers, Long examSubmissionId) {
        List<ExamAnswerResponse> answerResponses = examAnswers.stream()
            .map(ExamAnswerResponse::from)
            .toList();
        
        int totalCount = answerResponses.size();
        int correctCount = (int) answerResponses.stream()
            .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
            .count();
        int totalScore = answerResponses.stream()
            .mapToInt(answer -> answer.getScore() != null ? answer.getScore() : 0)
            .sum();
        
        return ExamAnswerListResponse.builder()
            .examSubmissionId(examSubmissionId)
            .answers(answerResponses)
            .totalCount(totalCount)
            .correctCount(correctCount)
            .totalScore(totalScore)
            .build();
    }
}


