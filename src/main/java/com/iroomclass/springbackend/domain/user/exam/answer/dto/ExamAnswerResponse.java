package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import com.iroomclass.springbackend.domain.exam.entity.StudentAnswerSheet;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

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
    @Schema(description = "답안 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID answerId,
    
    @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID examSubmissionId,
    
    @Schema(description = "문제 ID", example = "123e4567-e89b-12d3-a456-426614174002")
    UUID questionId,
    
    @Schema(description = "답안 이미지 URL (주관식 문제용)", example = "/uploads/answers/answer_1.jpg", nullable = true)
    String answerImageUrl,
    
    @Schema(description = "AI 인식 결과 (주관식 문제용)", example = "x = 5", nullable = true)
    String answerText,
    
    @Schema(description = "선택한 답안 번호 (객관식 문제용)", example = "2", nullable = true)
    Integer selectedChoice,
    
    @Schema(description = "AI 해답 처리 과정 (AI가 분석한 문제 해결 과정)", nullable = true)
    String aiSolutionProcess
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
     * 
     * NOTE: 채점 정보(정답 여부, 점수)는 QuestionResult에서 관리되므로 제외됨
     */
    public static ExamAnswerResponse from(StudentAnswerSheet studentAnswerSheet) {
        return new ExamAnswerResponse(
            studentAnswerSheet.getId(),
            studentAnswerSheet.getExamSubmission().getId(),
            studentAnswerSheet.getQuestion().getId(),
            studentAnswerSheet.getAnswerImageUrl(),
            studentAnswerSheet.getAnswerText(),
            studentAnswerSheet.getSelectedChoice(),
            studentAnswerSheet.getAiSolutionProcess()
        );
    }
}