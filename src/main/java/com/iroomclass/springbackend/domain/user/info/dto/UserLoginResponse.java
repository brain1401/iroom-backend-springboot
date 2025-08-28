package com.iroomclass.springbackend.domain.user.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * 학생 로그인 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 로그인 응답")
public record UserLoginResponse(
    @Schema(description = "학생 ID", example = "1")
    Long userId,

    @Schema(description = "학생 이름", example = "김철수")
    String name,

    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    String phone,

    @Schema(description = "로그인 성공 메시지", example = "로그인에 성공했습니다.")
    String message
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public UserLoginResponse {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
