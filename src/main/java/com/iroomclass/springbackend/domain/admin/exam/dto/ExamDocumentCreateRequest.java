package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 문서 생성 요청 DTO
 * 
 * 시험지 문서 생성 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 문서 생성 요청")
public record ExamDocumentCreateRequest(
    @NotNull(message = "시험지 ID는 필수입니다.")
    @Schema(description = "시험지 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID examSheetId
) {
    public ExamDocumentCreateRequest {
        Objects.requireNonNull(examSheetId, "examSheetId는 필수입니다");
    }
}