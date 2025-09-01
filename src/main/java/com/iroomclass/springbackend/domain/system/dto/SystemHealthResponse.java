package com.iroomclass.springbackend.domain.system.dto;

import com.iroomclass.springbackend.common.BaseRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 시스템 헬스체크 응답 DTO
 */
@Schema(name = "SystemHealthResponse", description = "시스템 헬스체크 응답")
public record SystemHealthResponse(
        @Schema(description = "전체 상태", example = "UP") 
        String status,
        
        @Schema(description = "타임스탬프", example = "2025-08-17T16:51:27") 
        LocalDateTime timestamp,
        
        @Schema(description = "전체 메시지", example = "모든 서비스가 정상적으로 작동중입니다") 
        String message,
        
        @Schema(description = "개별 서비스 상태") 
        Map<String, ServiceHealthResponse> services) implements BaseRecord {

    /**
     * 개별 서비스 헬스체크 정보
     */
    @Schema(name = "ServiceHealthResponse", description = "개별 서비스 헬스체크 응답")
    public record ServiceHealthResponse(
            @Schema(description = "서비스 상태", example = "UP") 
            String status,
            
            @Schema(description = "서비스 메시지", example = "데이터베이스 연결 정상") 
            String message,
            
            @Schema(description = "응답 시간 (ms)", example = "45", nullable = true) 
            Long responseTimeMs) implements BaseRecord {
        
        public static ServiceHealthResponse up(String message, Long responseTimeMs) {
            return new ServiceHealthResponse("UP", message, responseTimeMs);
        }
        
        public static ServiceHealthResponse down(String message) {
            return new ServiceHealthResponse("DOWN", message, null);
        }
    }

    /**
     * 정상 상태 헬스체크 응답 생성
     */
    public static SystemHealthResponse up(String message, Map<String, ServiceHealthResponse> services) {
        return new SystemHealthResponse("UP", LocalDateTime.now(), message, services);
    }

    /**
     * 오류 상태 헬스체크 응답 생성
     */
    public static SystemHealthResponse down(String message, Map<String, ServiceHealthResponse> services) {
        return new SystemHealthResponse("DOWN", LocalDateTime.now(), message, services);
    }

    /**
     * 정상 상태 헬스체크 응답 생성 (기존 호환성)
     */
    public static SystemHealthResponse up(String message) {
        return new SystemHealthResponse("UP", LocalDateTime.now(), message, Map.of());
    }

    /**
     * 오류 상태 헬스체크 응답 생성 (기존 호환성)
     */
    public static SystemHealthResponse down(String message) {
        return new SystemHealthResponse("DOWN", LocalDateTime.now(), message, Map.of());
    }
}