package com.iroomclass.spring_backend.domain.system.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 에코 요청 DTO
 */
public record EchoRequestDto(
        @Schema(description = "에코할 메시지", example = "hello") @NotBlank(message = "message는 필수입니다") String message) {
}
