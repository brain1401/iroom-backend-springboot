package com.iroomclass.springbackend.domain.admin.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 에러 응답 DTO
 * 
 * API 호출 시 발생하는 에러 정보를 담습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 응답 정보")
public class ErrorResponse {

    /**
     * 에러 메시지
     */
    @Schema(description = "에러 메시지", example = "존재하지 않는 관리자 아이디")
    private String message;
}
