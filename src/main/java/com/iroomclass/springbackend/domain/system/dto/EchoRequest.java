package com.iroomclass.springbackend.domain.system.dto;

import com.iroomclass.springbackend.common.BaseRecord;
import com.iroomclass.springbackend.common.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 에코 요청 DTO
 */
@Schema(name = "EchoRequest", description = "에코 요청")
public record EchoRequest(
        @Schema(description = "에코할 메시지", example = "hello", requiredMode = Schema.RequiredMode.REQUIRED) 
        @NotBlank(message = ValidationConstants.ECHO_MESSAGE_REQUIRED) 
        String message) implements BaseRecord {
}