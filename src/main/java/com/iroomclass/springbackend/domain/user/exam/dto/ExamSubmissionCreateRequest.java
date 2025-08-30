package com.iroomclass.springbackend.domain.user.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시험 제출 생성 요청 DTO
 * 
 * 학생이 시험을 제출할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "시험 제출 생성 요청")
public record ExamSubmissionCreateRequest(
    @NotNull(message = "시험 ID는 필수입니다.")
    @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID examId,
    
    @NotBlank(message = "학생 이름은 필수입니다.")
    @Schema(description = "학생 이름", example = "김철수", requiredMode = Schema.RequiredMode.REQUIRED)
    String studentName,
    
    @NotBlank(message = "학생 전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", 
             message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)")
    @Schema(description = "학생 전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    String studentPhone
) {
    public ExamSubmissionCreateRequest {
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
    }
}