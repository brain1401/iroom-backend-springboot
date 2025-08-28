package com.iroomclass.springbackend.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 표준 API 응답 래퍼
 */
@Schema(name = "ApiResponse", description = "표준 API 응답 래퍼")
public record ApiResponse<T>(
        @Schema(description = "응답 결과", requiredMode = Schema.RequiredMode.REQUIRED) ResultStatus result,
        @Schema(description = "응답 메시지") String message,
        @Schema(description = "응답 데이터") T data) {
    public ApiResponse {
        // result, message null 불가 검증 수행
        if (result == null)
            throw new IllegalArgumentException("result는 필수입니다");
        if (message == null)
            throw new IllegalArgumentException("message는 필수입니다");
    }

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultStatus.SUCCESS, message, data);
    }

    /**
     * 성공 응답 생성 (메시지 기본값)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultStatus.SUCCESS, "", data);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(ResultStatus.SUCCESS, message, null);
    }

    /**
     * 오류 응답 생성
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(ResultStatus.ERROR, message, null);
    }
}