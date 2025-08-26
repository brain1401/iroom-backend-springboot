package com.iroomclass.springbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 * 
 * 관리자 로그인 시 필요한 정보를 담습니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 정보")
public class LoginRequest {

    /**
     * 관리자 아이디
     */
    @Schema(description = "관리자 아이디", example = "admin", required = true)
    private String username;

    /**
     * 관리자 비밀번호
     */
    @Schema(description = "관리자 비밀번호", example = "admin123", required = true)
    private String password;
}
