package com.iroomclass.spring_backend.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API 응답 결과 상태 값
 */
@Schema(description = "API 응답 결과 상태 값")
public enum ResultStatus {
    @Schema(description = "요청 성공 상태")
    SUCCESS,
    @Schema(description = "요청 오류 상태")
    ERROR
}
