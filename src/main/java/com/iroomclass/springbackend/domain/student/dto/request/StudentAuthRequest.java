package com.iroomclass.springbackend.domain.student.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 학생 인증 요청 DTO
 * 
 * <p>학생의 신원 확인을 위한 3-factor 인증 정보를 담는 DTO입니다.
 * 이름, 생년월일, 전화번호를 통해 학생을 인증합니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 인증 요청 DTO", example = """
    {
      "name": "홍길동",
      "birthDate": "2000-01-01",
      "phone": "010-1234-5678"
    }
    """)
public record StudentAuthRequest(
    
    @NotBlank(message = "이름은 필수입니다")
    @Schema(description = "학생 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    
    @NotNull(message = "생년월일은 필수입니다")
    @Schema(description = "학생 생년월일", example = "2000-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate birthDate,
    
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
    @Schema(description = "학생 전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    String phone
    
) {
    /**
     * Compact Constructor - 입력값 검증
     */
    public StudentAuthRequest {
        Objects.requireNonNull(name, "이름은 필수입니다");
        Objects.requireNonNull(birthDate, "생년월일은 필수입니다");
        Objects.requireNonNull(phone, "전화번호는 필수입니다");
        
        if (name.isBlank()) {
            throw new IllegalArgumentException("이름은 공백일 수 없습니다");
        }
        
        if (phone.isBlank()) {
            throw new IllegalArgumentException("전화번호는 공백일 수 없습니다");
        }
    }
}