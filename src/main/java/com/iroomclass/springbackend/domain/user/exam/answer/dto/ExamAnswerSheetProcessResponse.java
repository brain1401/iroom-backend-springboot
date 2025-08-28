package com.iroomclass.springbackend.domain.user.exam.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

/**
 * 답안지 전체 촬영 처리 결과 응답 DTO
 * 
 * AI가 답안지 전체를 처리한 결과를 반환합니다.
 */
@Schema(description = "답안지 전체 촬영 처리 결과")
public record ExamAnswerSheetProcessResponse(
    @Schema(description = "처리된 이미지 개수", example = "2")
    Integer processedImageCount,
    
    @Schema(description = "생성된 답안 개수", example = "20")
    Integer createdAnswerCount,
    
    @Schema(description = "생성된 답안 목록")
    List<ExamAnswerResponse> answers,
    
    @Schema(description = "처리 상태", example = "COMPLETED")
    String status,
    
    @Schema(description = "처리 메시지", example = "답안지 처리가 완료되었습니다. 각 문제별 답안을 확인해주세요.")
    String message
) {
    public ExamAnswerSheetProcessResponse {
        Objects.requireNonNull(processedImageCount, "processedImageCount은 필수입니다");
        Objects.requireNonNull(createdAnswerCount, "createdAnswerCount은 필수입니다");
        Objects.requireNonNull(answers, "answers은 필수입니다");
        Objects.requireNonNull(status, "status은 필수입니다");
        Objects.requireNonNull(message, "message는 필수입니다");
    }
}