package com.iroomclass.springbackend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 학생 프로필 조회 응답 DTO
 * 
 * 마이페이지에서 학생의 기본 정보를 조회할 때 사용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학생 프로필 응답")
public record StudentProfileResponse(
        @Schema(description = "학생 이름", example = "김철수", requiredMode = Schema.RequiredMode.REQUIRED) String name,

        @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED) String phone,

        @Schema(description = "생년월일", example = "2008-03-15", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate birthDate,

        @Schema(description = "학년", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer grade) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public StudentProfileResponse {
        Objects.requireNonNull(name, "name은 필수입니다");
        Objects.requireNonNull(phone, "phone은 필수입니다");
        Objects.requireNonNull(birthDate, "birthDate는 필수입니다");
        Objects.requireNonNull(grade, "grade는 필수입니다");
    }
}