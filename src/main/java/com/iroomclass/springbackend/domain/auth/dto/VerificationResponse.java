package com.iroomclass.springbackend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검증 응답 DTO
 * 
 * <p>학생/선생님 검증 결과를 나타내는 간단한 true/false 응답 DTO입니다.</p>
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Schema(
    description = "검증 응답 DTO - 검증 성공/실패 여부",
    example = """
        {
          "verified": true
        }
        """
)
public record VerificationResponse(
    @Schema(description = "검증 성공 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean verified
) {
    
    /**
     * 검증 성공 응답 생성
     * 
     * @return 검증 성공 응답
     */
    public static VerificationResponse success() {
        return new VerificationResponse(true);
    }
    
    /**
     * 검증 실패 응답 생성
     * 
     * @return 검증 실패 응답
     */
    public static VerificationResponse failure() {
        return new VerificationResponse(false);
    }
}