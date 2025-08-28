package com.iroomclass.springbackend.domain.admin.exam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험지 초안 수정 요청 DTO
 * 
 * 시험지 초안 문제 교체 시 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험지 초안 수정 요청")
public record ExamDraftUpdateRequest(
    @NotNull(message = "교체할 문제 번호는 필수입니다.")
    @Min(value = 1, message = "문제 번호는 1 이상이어야 합니다.")
    @Schema(description = "교체할 문제 번호", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer seqNo
) {
    public ExamDraftUpdateRequest {
        Objects.requireNonNull(seqNo, "seqNo는 필수입니다");
    }
}