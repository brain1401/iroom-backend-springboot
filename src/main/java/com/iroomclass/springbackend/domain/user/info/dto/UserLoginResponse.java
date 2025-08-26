package com.iroomclass.springbackend.domain.user.info.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학생 로그인 응답 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 로그인 응답")
public class UserLoginResponse {

    @Schema(description = "학생 ID", example = "1")
    private Long userId;

    @Schema(description = "학생 이름", example = "김철수")
    private String name;

    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "로그인 성공 메시지", example = "로그인에 성공했습니다.")
    private String message;
}
