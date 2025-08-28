package com.iroomclass.springbackend.domain.system.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 에코 응답 DTO
 */
public record EchoDto(
        @Schema(description = "원본 메시지", example = "hello") String originalMessage,
        @Schema(description = "에코된 메시지", example = "hello") String echoMessage,
        @Schema(description = "처리 시간", example = "2024-06-01T12:34:56") LocalDateTime timestamp) {
    public EchoDto {
        Objects.requireNonNull(originalMessage, "originalMessage는 필수입니다");
        Objects.requireNonNull(echoMessage, "echoMessage는 필수입니다");
        Objects.requireNonNull(timestamp, "timestamp는 필수입니다");
    }
}
