package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 답안지 전체 촬영 요청 DTO
 * 
 * 학생이 답안지 전체를 촬영하여 AI가 모든 문제의 답안을 인식하도록 요청합니다.
 * 여러 페이지의 답안지가 있을 수 있으므로 이미지 URL 리스트를 받습니다.
 */
@Schema(description = "답안지 전체 촬영 요청")
public record ExamAnswerSheetCreateRequest(
    @NotNull(message = "시험 제출 ID는 필수입니다.")
    @Schema(description = "시험 제출 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID examSubmissionId,
    
    @NotEmpty(message = "답안지 이미지는 최소 1개 이상 필요합니다.")
    @Schema(description = "답안지 이미지 URL 목록 (여러 페이지 가능)", 
            example = "[\"https://example.com/answer-sheet-page1.jpg\", \"https://example.com/answer-sheet-page2.jpg\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    List<String> answerSheetImageUrls
) {
    public ExamAnswerSheetCreateRequest {
        Objects.requireNonNull(examSubmissionId, "examSubmissionId은 필수입니다");
        Objects.requireNonNull(answerSheetImageUrls, "answerSheetImageUrls은 필수입니다");
    }
}