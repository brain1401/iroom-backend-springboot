package com.iroomclass.springbackend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.iroomclass.springbackend.common.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.time.LocalDate;

/**
 * 학생 로그인 요청 DTO
 * 3-factor 인증: 이름 + 전화번호 + 생년월일
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 로그인 요청")
public record StudentLoginRequest(
        @NotBlank(message = ValidationConstants.REQUIRED_STUDENT_NAME) @Schema(description = "학생 이름", example = "김철수") String name,

        @NotBlank(message = ValidationConstants.REQUIRED_STUDENT_PHONE) @Pattern(regexp = ValidationConstants.PHONE_NUMBER_PATTERN, message = ValidationConstants.INVALID_PHONE_FORMAT) @Schema(description = "학생 전화번호", example = "010-1234-5678") String phone,

        @NotNull(message = ValidationConstants.REQUIRED_BIRTH_DATE) @Schema(description = "생년월일", example = "2008-03-15", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate birthDate) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public StudentLoginRequest {
        Objects.requireNonNull(name, "name은 필수입니다");
        Objects.requireNonNull(phone, "phone은 필수입니다");
        Objects.requireNonNull(birthDate, "birthDate는 필수입니다");
    }
}
