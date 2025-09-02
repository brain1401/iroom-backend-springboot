package com.iroomclass.springbackend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 학생 검증 요청 DTO
 * 
 * <p>학생의 이름, 전화번호, 생년월일을 통한 DB 매칭 검증을 위한 요청 DTO입니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    description = "학생 검증 요청 DTO - 3-factor 인증 (이름 + 전화번호 + 생년월일)",
    example = """
        {
          "name": "김철수",
          "phone": "010-1234-5678", 
          "birthDate": "2008-03-15"
        }
        """
)
public record VerifyStudentRequest(
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    @Schema(description = "학생 이름", example = "김철수", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다")
    @Schema(description = "학생 전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    String phone,
    
    @NotNull(message = "생년월일은 필수입니다")
    @Schema(description = "학생 생년월일", example = "2008-03-15", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate birthDate
) {
    
    /**
     * Compact Constructor - null 검증만 수행 (빈 값 검증은 Bean Validation에서 처리)
     */
    public VerifyStudentRequest {
        Objects.requireNonNull(name, "이름은 필수입니다");
        Objects.requireNonNull(phone, "전화번호는 필수입니다");
        Objects.requireNonNull(birthDate, "생년월일은 필수입니다");
    }
}