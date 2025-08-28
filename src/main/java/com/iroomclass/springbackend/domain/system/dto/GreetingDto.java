package com.iroomclass.springbackend.domain.system.dto;

import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인사 응답 DTO
 */
public record GreetingDto(
        @Schema(description = "이름", example = "홍길동") String name,
        @Schema(description = "인사 메시지", example = "Hello, 홍길동!") String message) {
    public GreetingDto {
        Objects.requireNonNull(name, "name은 필수입니다");
        Objects.requireNonNull(message, "message는 필수입니다");
    }
}
