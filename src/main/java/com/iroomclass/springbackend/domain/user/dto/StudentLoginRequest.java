package com.iroomclass.springbackend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
        @NotBlank(message = "학생 이름은 필수입니다.") @Schema(description = "학생 이름", example = "김철수") String name,

        @NotBlank(message = "학생 전화번호는 필수입니다.") @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다.") @Schema(description = "학생 전화번호", example = "010-1234-5678") String phone,

        @NotNull(message = "생년월일은 필수입니다.") @Schema(description = "생년월일", example = "2008-03-15", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate birthDate) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public StudentLoginRequest {
        Objects.requireNonNull(name, "name은 필수입니다");
        Objects.requireNonNull(phone, "phone은 필수입니다");
        Objects.requireNonNull(birthDate, "birthDate는 필수입니다");
    }
}
