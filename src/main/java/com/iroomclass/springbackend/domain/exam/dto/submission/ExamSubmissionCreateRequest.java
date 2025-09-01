package com.iroomclass.springbackend.domain.exam.dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.iroomclass.springbackend.common.ValidationConstants;
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
        @NotNull(message = "시험 ID는 필수입니다.") @Schema(description = "시험 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) UUID examId,

        @NotBlank(message = ValidationConstants.REQUIRED_STUDENT_NAME) @Schema(description = "학생 이름", example = "김철수", requiredMode = Schema.RequiredMode.REQUIRED) String studentName,

        @NotBlank(message = ValidationConstants.REQUIRED_STUDENT_PHONE) @Pattern(regexp = ValidationConstants.PHONE_NUMBER_PATTERN, message = ValidationConstants.INVALID_PHONE_FORMAT) @Schema(description = "학생 전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED) String studentPhone) {
    public ExamSubmissionCreateRequest {
        Objects.requireNonNull(examId, "examId은 필수입니다");
        Objects.requireNonNull(studentName, "studentName는 필수입니다");
        Objects.requireNonNull(studentPhone, "studentPhone는 필수입니다");
    }
}