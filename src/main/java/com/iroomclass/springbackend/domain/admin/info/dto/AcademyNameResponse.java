package com.iroomclass.springbackend.domain.admin.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * 학원명 응답 DTO
 * 
 * 학원명 조회 결과를 담습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(description = "학원명 응답 정보")
public record AcademyNameResponse(
    @Schema(description = "학원명", example = "이룸클래스")
    String academyName
) {
    /**
     * Compact Constructor - 입력 검증 수행
     */
    public AcademyNameResponse {
        Objects.requireNonNull(academyName, "academyName는 필수입니다");
    }
}