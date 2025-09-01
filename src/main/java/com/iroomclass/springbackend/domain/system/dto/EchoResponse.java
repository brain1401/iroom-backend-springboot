package com.iroomclass.springbackend.domain.system.dto;

import com.iroomclass.springbackend.common.BaseRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 에코 응답 DTO
 */
@Schema(name = "EchoResponse", description = "에코 응답")
public record EchoResponse(
        @Schema(description = "원본 메시지", example = "hello") 
        String originalMessage,
        
        @Schema(description = "에코된 메시지", example = "hello") 
        String echoMessage,
        
        @Schema(description = "처리 시간", example = "2024-06-01T12:34:56") 
        LocalDateTime timestamp) implements BaseRecord {
    
    public EchoResponse {
        requireNonNull(originalMessage, "originalMessage");
        requireNonNull(echoMessage, "echoMessage");
        requireNonNull(timestamp, "timestamp");
    }
}