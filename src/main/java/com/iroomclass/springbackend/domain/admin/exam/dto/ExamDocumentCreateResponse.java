package com.iroomclass.springbackend.domain.admin.exam.dto;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 문서 생성 응답 DTO
 * 
 * 시험지 문서 생성 완료 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 문서 생성 응답")
public record ExamDocumentCreateResponse(
    @Schema(description = "시험지 ID", example = "1")
    Long examSheetId,
    
    @Schema(description = "시험지 이름", example = "1학년 중간고사")
    String examName,
    
    @Schema(description = "학년", example = "1")
    Integer grade,
    
    @Schema(description = "총 문제 개수", example = "20")
    Integer totalQuestions,
    
    @Schema(description = "생성된 문서 개수", example = "3")
    Integer documentCount
) {
    public ExamDocumentCreateResponse {
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
        Objects.requireNonNull(examName, "examName은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(totalQuestions, "totalQuestions는 필수입니다");
        Objects.requireNonNull(documentCount, "documentCount는 필수입니다");
    }
}