package com.iroomclass.springbackend.domain.system.dto;

import com.iroomclass.springbackend.common.BaseRecord;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인사 응답 DTO
 */
@Schema(name = "GreetingResponse", description = "인사 응답")
public record GreetingResponse(
        @Schema(description = "이름", example = "홍길동") 
        String name,
        
        @Schema(description = "인사 메시지", example = "Hello, 홍길동!") 
        String message) implements BaseRecord {
        
    public GreetingResponse {
        requireNonNull(name, "name");
        requireNonNull(message, "message");
    }
}