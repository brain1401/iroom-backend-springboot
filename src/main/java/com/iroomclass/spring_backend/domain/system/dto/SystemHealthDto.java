package com.iroomclass.spring_backend.domain.system.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시스템 헬스체크 응답 DTO
 */
public record SystemHealthDto(
        @Schema(description = "상태", example = "UP") String status,
        @Schema(description = "타임스탬프", example = "2024-06-01T12:34:56") LocalDateTime timestamp,
        @Schema(description = "메시지", example = "정상") String message) {
    /**
     * 정상 상태 헬스체크 응답 생성
     * 
     * @param message 상태 메시지
     * @return 헬스체크 DTO
     */
    public static SystemHealthDto up(String message) {
        return new SystemHealthDto("UP", LocalDateTime.now(), message);
    }

    /**
     * 오류 상태 헬스체크 응답 생성
     * 
     * @param message 오류 메시지
     * @return 헬스체크 DTO
     */
    public static SystemHealthDto down(String message) {
        return new SystemHealthDto("DOWN", LocalDateTime.now(), message);
    }
}
