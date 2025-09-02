package com.iroomclass.springbackend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 선생님 검증 요청 DTO
 * 
 * <p>선생님의 사용자명, 비밀번호를 통한 DB 매칭 검증을 위한 요청 DTO입니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    description = "선생님 검증 요청 DTO - 사용자명 + 비밀번호",
    example = """
        {
          "username": "admin",
          "password": "admin123"
        }
        """
)
public record VerifyTeacherRequest(
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(max = 50, message = "사용자명은 50자 이하여야 합니다")
    @Schema(description = "선생님 사용자명", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max = 100, message = "비밀번호는 4-100자여야 합니다")
    @Schema(description = "선생님 비밀번호", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
    
    /**
     * Compact Constructor - null 검증만 수행 (빈 값 검증은 Bean Validation에서 처리)
     */
    public VerifyTeacherRequest {
        Objects.requireNonNull(username, "사용자명은 필수입니다");
        Objects.requireNonNull(password, "비밀번호는 필수입니다");
    }
}