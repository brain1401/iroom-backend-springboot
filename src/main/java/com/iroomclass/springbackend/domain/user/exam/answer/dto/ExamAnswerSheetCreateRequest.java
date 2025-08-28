package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 답안지 전체 촬영 요청 DTO
 * 
 * 학생이 답안지 전체를 촬영하여 AI가 모든 문제의 답안을 인식하도록 요청합니다.
 * 여러 페이지의 답안지가 있을 수 있으므로 이미지 URL 리스트를 받습니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "답안지 전체 촬영 요청")
public class ExamAnswerSheetCreateRequest {

    @Schema(description = "시험 제출 ID", example = "1")
    private Long examSubmissionId;

    @Schema(description = "답안지 이미지 URL 목록 (여러 페이지 가능)", 
            example = "[\"https://example.com/answer-sheet-page1.jpg\", \"https://example.com/answer-sheet-page2.jpg\"]")
    private List<String> answerSheetImageUrls;
}
