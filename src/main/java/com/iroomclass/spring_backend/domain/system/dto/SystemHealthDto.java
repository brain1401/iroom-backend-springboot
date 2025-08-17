package com.iroomclass.spring_backend.domain.system.dto;

import java.time.LocalDateTime;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시스템 헬스체크 응답 DTO
 */
public record SystemHealthDto(
        @Schema(description = "전체 상태", example = "UP") String status,
        @Schema(description = "타임스탬프", example = "2025-08-17T16:51:27") LocalDateTime timestamp,
        @Schema(description = "전체 메시지", example = "모든 서비스가 정상적으로 작동중입니다") String message,
        @Schema(description = "개별 서비스 상태", 
                example = "{\"application\":{\"status\":\"UP\",\"message\":\"Spring Boot 애플리케이션 정상\",\"responseTimeMs\":0},\"database\":{\"status\":\"UP\",\"message\":\"데이터베이스 연결 정상\",\"responseTimeMs\":45},\"aiServer\":{\"status\":\"UP\",\"message\":\"AI 서버 연결 정상\",\"responseTimeMs\":120}}") 
        Map<String, ServiceHealthDto> services) {

    /**
     * 개별 서비스 헬스체크 정보
     */
    public record ServiceHealthDto(
            @Schema(description = "서비스 상태", example = "UP") String status,
            @Schema(description = "서비스 메시지", example = "데이터베이스 연결 정상") String message,
            @Schema(description = "응답 시간 (ms)", example = "45", nullable = true) Long responseTimeMs) {
        
        public static ServiceHealthDto up(String message, Long responseTimeMs) {
            return new ServiceHealthDto("UP", message, responseTimeMs);
        }
        
        public static ServiceHealthDto down(String message) {
            return new ServiceHealthDto("DOWN", message, null);
        }
    }

    /**
     * 정상 상태 헬스체크 응답 생성
     * 
     * @param message 상태 메시지
     * @param services 개별 서비스 상태
     * @return 헬스체크 DTO
     */
    public static SystemHealthDto up(String message, Map<String, ServiceHealthDto> services) {
        return new SystemHealthDto("UP", LocalDateTime.now(), message, services);
    }

    /**
     * 오류 상태 헬스체크 응답 생성
     * 
     * @param message 오류 메시지
     * @param services 개별 서비스 상태
     * @return 헬스체크 DTO
     */
    public static SystemHealthDto down(String message, Map<String, ServiceHealthDto> services) {
        return new SystemHealthDto("DOWN", LocalDateTime.now(), message, services);
    }

    /**
     * 정상 상태 헬스체크 응답 생성 (기존 호환성)
     * 
     * @param message 상태 메시지
     * @return 헬스체크 DTO
     */
    public static SystemHealthDto up(String message) {
        return new SystemHealthDto("UP", LocalDateTime.now(), message, Map.of());
    }

    /**
     * 오류 상태 헬스체크 응답 생성 (기존 호환성)
     * 
     * @param message 오류 메시지
     * @return 헬스체크 DTO
     */
    public static SystemHealthDto down(String message) {
        return new SystemHealthDto("DOWN", LocalDateTime.now(), message, Map.of());
    }
}
