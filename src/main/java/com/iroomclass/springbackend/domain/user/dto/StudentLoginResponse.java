package com.iroomclass.springbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.util.UUID;
import java.time.LocalDate;

/**
 * 학생 로그인 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 로그인 응답")
public record StudentLoginResponse(
        @Schema(description = "학생 ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId,

        @Schema(description = "학생 이름", example = "김철수") String name,

        @Schema(description = "학생 전화번호", example = "010-1234-5678") String phone,

        @Schema(description = "학년", example = "1") Integer grade,

        @Schema(description = "생년월일", example = "2008-03-15") LocalDate birthDate,

        @Schema(description = "로그인 성공 메시지", example = "로그인에 성공했습니다.") String message) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public StudentLoginResponse {
        Objects.requireNonNull(userId, "userId는 필수입니다");
        Objects.requireNonNull(name, "name은 필수입니다");
        Objects.requireNonNull(phone, "phone은 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
        Objects.requireNonNull(birthDate, "birthDate는 필수입니다");
        Objects.requireNonNull(message, "message는 필수입니다");
    }
}
