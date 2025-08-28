package com.iroomclass.springbackend.domain.user.info.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 학생 로그인 요청 DTO
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 로그인 요청")
public class UserLoginRequest {

    @NotBlank(message = "학생 이름은 필수입니다.")
    @Schema(description = "학생 이름", example = "김철수")
    private String name;

    @NotBlank(message = "학생 전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    @Schema(description = "학생 전화번호", example = "010-1234-5678")
    private String phone;
}
