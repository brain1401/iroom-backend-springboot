package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 답안지 전체 촬영 처리 결과 응답 DTO
 * 
 * AI가 답안지 전체를 처리한 결과를 반환합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "답안지 전체 촬영 처리 결과")
public class ExamAnswerSheetProcessResponse {

    @Schema(description = "처리된 이미지 개수", example = "2")
    private Integer processedImageCount;

    @Schema(description = "생성된 답안 개수", example = "20")
    private Integer createdAnswerCount;

    @Schema(description = "생성된 답안 목록")
    private List<ExamAnswerResponse> answers;

    @Schema(description = "처리 상태", example = "COMPLETED")
    private String status;

    @Schema(description = "처리 메시지", example = "답안지 처리가 완료되었습니다. 각 문제별 답안을 확인해주세요.")
    private String message;
}
