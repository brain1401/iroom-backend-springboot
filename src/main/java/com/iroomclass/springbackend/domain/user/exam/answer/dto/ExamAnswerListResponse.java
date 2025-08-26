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

/**
 * 전체 답안지 처리 결과 응답 DTO
 * 
 * 전체 답안지 촬영 후 AI가 각 문제별로 추출한 결과를 전달할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 답안지 처리 결과 응답")
class ExamAnswerSheetProcessResponse {
    
    @Schema(description = "시험 제출 ID", example = "1")
    private Long examSubmissionId;
    
    @Schema(description = "처리된 답안 목록")
    private List<ExamAnswerResponse> processedAnswers;
    
    @Schema(description = "총 처리된 답안 수", example = "20")
    private int totalProcessedCount;
    
    @Schema(description = "성공적으로 인식된 답안 수", example = "18")
    private int successfullyRecognizedCount;
    
    @Schema(description = "인식 실패한 답안 수", example = "2")
    private int failedRecognitionCount;
    
    @Schema(description = "인식 실패한 문제 번호 목록", example = "[3, 7]")
    private List<Integer> failedQuestionNumbers;
    
    @Schema(description = "처리된 답안지 이미지 개수", example = "3")
    private int processedImageCount;
}
